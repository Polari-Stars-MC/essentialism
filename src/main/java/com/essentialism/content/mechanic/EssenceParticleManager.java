package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.EssenceLevel;
import com.essentialism.content.essence.EssenceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages ambient particle effects for essence-related phenomena.
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class EssenceParticleManager {

    private static final Map<BlockPos, ParticleEmitter> EMITTERS = new ConcurrentHashMap<>();
    private static final int PARTICLE_INTERVAL = 5;
    /** Tracks last tick per level to avoid cross-dimension interference */
    private static final Map<Level, Long> LAST_TICK = new java.util.WeakHashMap<>();

    private EssenceParticleManager() {}

    /**
     * Spawn essence-level-appropriate particles at a position.
     */
    public static void spawnEssenceParticles(Level level, BlockPos pos, EssenceType type, float concentration) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        EssenceLevel tier = EssenceLevel.of(concentration);

        int count = switch (tier) {
            case TRACE -> 1;
            case LOW -> 3;
            case MEDIUM -> 6;
            case HIGH -> 10;
            case EXTREME -> 15;
            case INFINITE -> 20;
            default -> 0;
        };

        if (count == 0) return;

        double spread = 0.3 + count * 0.04;
        double speed = 0.01 + count * 0.005;

        // Use dust particles as base visual
        serverLevel.sendParticles(
                ParticleTypes.DUST_PLUME,
                pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                count, spread, 0.4, spread, speed
        );

        // Add sparks at higher tiers
        if (tier.ordinal() >= EssenceLevel.MEDIUM.ordinal()) {
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    count / 2, spread * 0.8, 0.3, spread * 0.8, speed * 2
            );
        }

        // Add glow at high+
        if (tier.ordinal() >= EssenceLevel.HIGH.ordinal()) {
            serverLevel.sendParticles(
                    ParticleTypes.GLOW,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    count / 3, spread * 0.5, 0.2, spread * 0.5, 0
            );
        }

        // Add end rod spark at extreme+
        if (tier.ordinal() >= EssenceLevel.EXTREME.ordinal()) {
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    count / 4, spread * 0.4, 0.2, spread * 0.4, 0.02
            );
        }
    }

    public static void addEmitter(BlockPos pos, EssenceType type, float concentration) {
        EMITTERS.put(pos.immutable(), new ParticleEmitter(type, concentration));
    }

    public static void removeEmitter(BlockPos pos) {
        EMITTERS.remove(pos);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        Level level = event.getLevel();

        long gameTime = level.getGameTime();
        Long last = LAST_TICK.get(level);
        if (last != null && gameTime - last < PARTICLE_INTERVAL) return;
        LAST_TICK.put(level, gameTime);

        Iterator<Map.Entry<BlockPos, ParticleEmitter>> iter = EMITTERS.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, ParticleEmitter> entry = iter.next();
            BlockPos pos = entry.getKey();
            ParticleEmitter emitter = entry.getValue();

            if (!level.hasChunkAt(pos)) continue;
            spawnEssenceParticles(level, pos, emitter.type, emitter.concentration);
        }
    }

    private record ParticleEmitter(EssenceType type, float concentration) {}
}
