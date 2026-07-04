package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.essence.EssenceType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks essence accumulation per chunk and applies resonance effects
 * when thresholds are exceeded.
 * <p>
 * Based on the design document's "Essence Resonance (Area Effects)" table.
 * Each essence type has a threshold; when the total in a chunk exceeds it,
 * all players in that chunk receive a corresponding effect.
 * <p>
 * Natural decay: all essence values decay by 0.01% per tick,
 * so effects fade naturally when extraction stops.
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class EssenceResonance {

    /** Decay factor per tick (0.9999 = 0.01% decay per tick) */
    private static final float DECAY_PER_TICK = 0.9999F;

    /** How often (in ticks) to apply resonance effects */
    private static final int EFFECT_INTERVAL = 40; // every 2 seconds

    /** How often (in ticks) to run full decay pass */
    private static final int DECAY_INTERVAL = 100; // every 5 seconds

    /** Thresholds for each essence type */
    private static final Map<EssenceType, Integer> THRESHOLDS = Map.of(
            EssenceType.SOLIDITY, 5000,
            EssenceType.LIFE, 4000,
            EssenceType.DECAY, 3000,
            EssenceType.LIGHT, 3500,
            EssenceType.SHADOW, 3500,
            EssenceType.MOTION, 4000,
            EssenceType.MIND, 2500,
            EssenceType.SPACETIME, 2000,
            EssenceType.RESONANCE, 3000
    );

    /**
     * Chunk → (EssenceType → accumulated float value).
     * Only stored on the server side.
     */
    private static final Map<ChunkPos, Map<EssenceType, Float>> CHUNK_ESSENCE = new ConcurrentHashMap<>();

    /**
     * Timestamp of last decay tick, per level.
     */
    private static final Map<Level, Long> LAST_DECAY = new WeakHashMap<>();

    /**
     * Timestamp of last effect tick, per level.
     */
    private static final Map<Level, Long> LAST_EFFECT = new WeakHashMap<>();

    private EssenceResonance() {}

    // ─── Event handlers ────────────────────────────────────────────────

    /**
     * When a block is broken, add its essence profile values to the chunk total.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;
        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();

        EssenceProfile profile = EssenceProfiles.get(level, pos);
        if (profile == null) return;

        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);
        Map<EssenceType, Float> chunkValues = CHUNK_ESSENCE.computeIfAbsent(
                chunkPos, k -> new ConcurrentHashMap<>());

        for (EssenceType type : EssenceType.values()) {
            float value = profile.get(type);
            if (value > 0) {
                chunkValues.merge(type, value, Float::sum);
            }
        }
    }

    /**
     * Periodic decay and effect application on server tick.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        Level level = event.getLevel();

        long gameTime = level.getGameTime();

        // Decay pass
        Long lastDecay = LAST_DECAY.get(level);
        if (lastDecay == null || gameTime - lastDecay >= DECAY_INTERVAL) {
            LAST_DECAY.put(level, gameTime);
            applyDecay();
        }

        // Effect pass
        Long lastEffect = LAST_EFFECT.get(level);
        if (lastEffect == null || gameTime - lastEffect >= EFFECT_INTERVAL) {
            LAST_EFFECT.put(level, gameTime);
            applyEffects(level);
        }
    }

    // ─── Decay logic ───────────────────────────────────────────────────

    private static void applyDecay() {
        Iterator<Map.Entry<ChunkPos, Map<EssenceType, Float>>> chunkIter = CHUNK_ESSENCE.entrySet().iterator();
        while (chunkIter.hasNext()) {
            Map.Entry<ChunkPos, Map<EssenceType, Float>> entry = chunkIter.next();
            Map<EssenceType, Float> values = entry.getValue();
            boolean allZero = true;

            for (Iterator<Map.Entry<EssenceType, Float>> typeIter = values.entrySet().iterator(); typeIter.hasNext(); ) {
                Map.Entry<EssenceType, Float> typeEntry = typeIter.next();
                float decayed = typeEntry.getValue() * DECAY_PER_TICK;
                if (decayed < 0.5F) {
                    typeIter.remove(); // drop to zero
                } else {
                    typeEntry.setValue(decayed);
                    allZero = false;
                }
            }

            if (allZero) {
                chunkIter.remove();
            }
        }
    }

    // ─── Effect logic ──────────────────────────────────────────────────

    private static void applyEffects(Level level) {
        for (Player player : level.players()) {
            if (!(player instanceof ServerPlayer sp)) continue;
            ChunkPos chunkPos = new ChunkPos(player.blockPosition().getX() >> 4, player.blockPosition().getZ() >> 4);
            Map<EssenceType, Float> chunkValues = CHUNK_ESSENCE.get(chunkPos);
            if (chunkValues == null || chunkValues.isEmpty()) continue;

            for (Map.Entry<EssenceType, Integer> thresholdEntry : THRESHOLDS.entrySet()) {
                EssenceType type = thresholdEntry.getKey();
                int threshold = thresholdEntry.getValue();
                Float accumulated = chunkValues.get(type);
                if (accumulated == null || accumulated < threshold) continue;

                applyEffectForType(sp, type);
            }
        }
    }

    private static void applyEffectForType(ServerPlayer player, EssenceType type) {
        switch (type) {
            case SOLIDITY -> {
                // 所有方块硬度 +1，挖掘时间 +20% → Mining Fatigue I
                player.addEffect(new MobEffectInstance(
                        MobEffects.MINING_FATIGUE, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case LIFE -> {
                // 植物生长速度 x2 → Regeneration as marker
                player.addEffect(new MobEffectInstance(
                        MobEffects.REGENERATION, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case DECAY -> {
                // 物品耐久损耗 +50% → Weakness
                player.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case LIGHT -> {
                // 禁止自然怪物生成 + 玩家夜视
                player.addEffect(new MobEffectInstance(
                        MobEffects.NIGHT_VISION, EFFECT_INTERVAL + 100, 0,
                        false, false, true));
            }
            case SHADOW -> {
                // 光照等级 -5，幻翼生成率提高 → Darkness
                player.addEffect(new MobEffectInstance(
                        MobEffects.DARKNESS, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case MOTION -> {
                // 玩家移速 +10% → Speed I
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case MIND -> {
                // 经验掉落 +50%，交易折扣 → Luck
                player.addEffect(new MobEffectInstance(
                        MobEffects.LUCK, EFFECT_INTERVAL + 100, 0,
                        false, false, true));
            }
            case SPACETIME -> {
                // 随机传送粒子/异响 → Slow Falling as marker
                player.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
            case RESONANCE -> {
                // 红石信号无衰减 → Glowing as visual marker
                player.addEffect(new MobEffectInstance(
                        MobEffects.GLOWING, EFFECT_INTERVAL + 20, 0,
                        false, false, true));
            }
        }
    }

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Returns the total accumulated essence of a given type in a chunk.
     */
    public static float getChunkEssence(ChunkPos chunkPos, EssenceType type) {
        Map<EssenceType, Float> values = CHUNK_ESSENCE.get(chunkPos);
        if (values == null) return 0F;
        return values.getOrDefault(type, 0F);
    }

    /**
     * Returns the threshold for a given essence type.
     */
    public static int getThreshold(EssenceType type) {
        return THRESHOLDS.getOrDefault(type, 0);
    }

    /**
     * Returns whether the given chunk has any essence exceeding its threshold.
     */
    public static boolean isResonating(ChunkPos chunkPos) {
        Map<EssenceType, Float> values = CHUNK_ESSENCE.get(chunkPos);
        if (values == null) return false;
        for (Map.Entry<EssenceType, Float> entry : values.entrySet()) {
            int threshold = THRESHOLDS.getOrDefault(entry.getKey(), Integer.MAX_VALUE);
            if (entry.getValue() >= threshold) return true;
        }
        return false;
    }

    /**
     * Returns a copy of all chunk essence data (for spectrometer display).
     */
    public static Map<EssenceType, Float> getChunkEssenceSnapshot(ChunkPos chunkPos) {
        Map<EssenceType, Float> values = CHUNK_ESSENCE.get(chunkPos);
        if (values == null) return Map.of();
        return new HashMap<>(values);
    }
}
