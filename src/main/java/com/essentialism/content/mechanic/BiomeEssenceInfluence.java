package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.EssenceType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Applies biome-specific essence multipliers to chunk resonance accumulation.
 * <p>
 * Each biome has innate essence affinities — swamps overflow with decay,
 * deserts radiate motion, deep dark pulses with shadow and mind.
 * This makes certain biomes naturally attuned to specific essence types,
 * amplifying accumulation or increasing depletion recovery rates.
 * <p>
 * Multipliers stack with chunk resonance thresholds:
 * - Biomes with high decay affinity reach decay resonance faster
 * - Biomes with high life affinity recover from depletion faster
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class BiomeEssenceInfluence {

    /** Per-biome essence affinity multipliers (>1 = boosted, <1 = suppressed) */
    private static final Map<ResourceKey<Biome>, Map<EssenceType, Float>> BIOME_AFFINITIES = new HashMap<>();

    static {
        // ─── Overworld ─────────────────────────────────────────────────
        putAffinity(Biomes.DARK_FOREST,
                EssenceType.SHADOW, 1.5F, EssenceType.LIFE, 1.2F, EssenceType.DECAY, 1.3F);
        putAffinity(Biomes.SWAMP,
                EssenceType.DECAY, 1.8F, EssenceType.LIFE, 1.3F, EssenceType.SHADOW, 1.2F);
        putAffinity(Biomes.MANGROVE_SWAMP,
                EssenceType.DECAY, 1.6F, EssenceType.LIFE, 1.4F, EssenceType.SHADOW, 1.1F);
        putAffinity(Biomes.DESERT,
                EssenceType.MOTION, 1.5F, EssenceType.DECAY, 1.3F, EssenceType.LIGHT, 1.2F);
        putAffinity(Biomes.BADLANDS,
                EssenceType.SOLIDITY, 1.3F, EssenceType.DECAY, 1.2F, EssenceType.MOTION, 1.2F);
        putAffinity(Biomes.JUNGLE,
                EssenceType.LIFE, 1.6F, EssenceType.DECAY, 1.2F);
        putAffinity(Biomes.BAMBOO_JUNGLE,
                EssenceType.LIFE, 1.5F, EssenceType.RESONANCE, 1.2F);
        putAffinity(Biomes.SPARSE_JUNGLE,
                EssenceType.LIFE, 1.3F, EssenceType.MOTION, 1.2F);
        putAffinity(Biomes.FLOWER_FOREST,
                EssenceType.LIFE, 1.5F, EssenceType.RESONANCE, 1.3F, EssenceType.LIGHT, 1.1F);
        putAffinity(Biomes.MEADOW,
                EssenceType.LIFE, 1.3F, EssenceType.RESONANCE, 1.1F, EssenceType.LIGHT, 1.1F);
        putAffinity(Biomes.CHERRY_GROVE,
                EssenceType.LIFE, 1.4F, EssenceType.RESONANCE, 1.2F);
        putAffinity(Biomes.TAIGA,
                EssenceType.LIFE, 1.2F, EssenceType.SOLIDITY, 1.1F);
        putAffinity(Biomes.OLD_GROWTH_PINE_TAIGA,
                EssenceType.LIFE, 1.3F, EssenceType.SOLIDITY, 1.3F, EssenceType.SPACETIME, 1.1F);
        putAffinity(Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                EssenceType.LIFE, 1.3F, EssenceType.SOLIDITY, 1.2F, EssenceType.SHADOW, 1.1F);
        putAffinity(Biomes.SNOWY_TAIGA,
                EssenceType.SOLIDITY, 1.2F, EssenceType.MOTION, 1.1F);
        putAffinity(Biomes.SUNFLOWER_PLAINS,
                EssenceType.LIGHT, 1.3F, EssenceType.LIFE, 1.1F);

        // ─── Mountains ─────────────────────────────────────────────────
        putAffinity(Biomes.WINDSWEPT_HILLS,
                EssenceType.MOTION, 1.4F, EssenceType.SOLIDITY, 1.1F);
        putAffinity(Biomes.WINDSWEPT_FOREST,
                EssenceType.MOTION, 1.2F, EssenceType.LIFE, 1.1F);
        putAffinity(Biomes.WINDSWEPT_GRAVELLY_HILLS,
                EssenceType.MOTION, 1.3F, EssenceType.SOLIDITY, 1.2F);
        putAffinity(Biomes.WINDSWEPT_SAVANNA,
                EssenceType.MOTION, 1.3F, EssenceType.LIFE, 1.1F);
        putAffinity(Biomes.JAGGED_PEAKS,
                EssenceType.SOLIDITY, 1.5F, EssenceType.MOTION, 1.2F, EssenceType.LIGHT, 1.1F);
        putAffinity(Biomes.FROZEN_PEAKS,
                EssenceType.SOLIDITY, 1.5F, EssenceType.MOTION, 1.1F);
        putAffinity(Biomes.STONY_PEAKS,
                EssenceType.SOLIDITY, 1.6F, EssenceType.SPACETIME, 1.1F);

        // ─── Aquatic ───────────────────────────────────────────────────
        putAffinity(Biomes.OCEAN,
                EssenceType.MOTION, 1.2F, EssenceType.RESONANCE, 1.1F);
        putAffinity(Biomes.DEEP_OCEAN,
                EssenceType.MOTION, 1.1F, EssenceType.SHADOW, 1.3F, EssenceType.SPACETIME, 1.2F);
        putAffinity(Biomes.WARM_OCEAN,
                EssenceType.LIFE, 1.3F, EssenceType.LIGHT, 1.1F);
        putAffinity(Biomes.LUKEWARM_OCEAN,
                EssenceType.LIFE, 1.2F, EssenceType.RESONANCE, 1.1F);
        putAffinity(Biomes.COLD_OCEAN,
                EssenceType.MOTION, 1.2F, EssenceType.SOLIDITY, 1.1F);
        putAffinity(Biomes.DEEP_COLD_OCEAN,
                EssenceType.SHADOW, 1.3F, EssenceType.SPACETIME, 1.2F);
        putAffinity(Biomes.DEEP_FROZEN_OCEAN,
                EssenceType.SHADOW, 1.2F, EssenceType.SPACETIME, 1.3F);
        putAffinity(Biomes.DEEP_LUKEWARM_OCEAN,
                EssenceType.SHADOW, 1.2F, EssenceType.SPACETIME, 1.1F);

        // ─── Underground / Cave ────────────────────────────────────────
        putAffinity(Biomes.DRIPSTONE_CAVES,
                EssenceType.SOLIDITY, 1.3F, EssenceType.SPACETIME, 1.2F);
        putAffinity(Biomes.LUSH_CAVES,
                EssenceType.LIFE, 1.4F, EssenceType.LIGHT, 1.2F, EssenceType.RESONANCE, 1.1F);
        putAffinity(Biomes.DEEP_DARK,
                EssenceType.SHADOW, 2.0F, EssenceType.MIND, 1.8F, EssenceType.SPACETIME, 1.3F);

        // ─── Nether ────────────────────────────────────────────────────
        putAffinity(Biomes.NETHER_WASTES,
                EssenceType.DECAY, 1.5F, EssenceType.LIGHT, 1.1F, EssenceType.SOLIDITY, 1.1F);
        putAffinity(Biomes.SOUL_SAND_VALLEY,
                EssenceType.DECAY, 1.6F, EssenceType.SHADOW, 1.4F, EssenceType.MIND, 1.3F);
        putAffinity(Biomes.CRIMSON_FOREST,
                EssenceType.LIFE, 1.4F, EssenceType.DECAY, 1.2F, EssenceType.RESONANCE, 1.2F);
        putAffinity(Biomes.WARPED_FOREST,
                EssenceType.MIND, 1.5F, EssenceType.SPACETIME, 1.3F, EssenceType.RESONANCE, 1.1F);
        putAffinity(Biomes.BASALT_DELTAS,
                EssenceType.SOLIDITY, 1.5F, EssenceType.DECAY, 1.3F, EssenceType.MOTION, 1.2F);

        // ─── End ───────────────────────────────────────────────────────
        putAffinity(Biomes.THE_END,
                EssenceType.SPACETIME, 1.8F, EssenceType.SHADOW, 1.3F);
        putAffinity(Biomes.END_HIGHLANDS,
                EssenceType.SPACETIME, 2.0F, EssenceType.LIGHT, 1.2F, EssenceType.SHADOW, 1.2F);
        putAffinity(Biomes.END_MIDLANDS,
                EssenceType.SPACETIME, 1.6F, EssenceType.SHADOW, 1.1F);
        putAffinity(Biomes.SMALL_END_ISLANDS,
                EssenceType.SPACETIME, 1.5F, EssenceType.MOTION, 1.2F);
        putAffinity(Biomes.END_BARRENS,
                EssenceType.SPACETIME, 1.4F, EssenceType.SHADOW, 1.4F);
    }

    private static void putAffinity(ResourceKey<Biome> biome, EssenceType t1, float v1) {
        BIOME_AFFINITIES.computeIfAbsent(biome, k -> new EnumMap<>(EssenceType.class))
                .put(t1, v1);
    }

    private static void putAffinity(ResourceKey<Biome> biome,
            EssenceType t1, float v1, EssenceType t2, float v2) {
        var map = BIOME_AFFINITIES.computeIfAbsent(biome, k -> new EnumMap<>(EssenceType.class));
        map.put(t1, v1);
        map.put(t2, v2);
    }

    private static void putAffinity(ResourceKey<Biome> biome,
            EssenceType t1, float v1, EssenceType t2, float v2, EssenceType t3, float v3) {
        var map = BIOME_AFFINITIES.computeIfAbsent(biome, k -> new EnumMap<>(EssenceType.class));
        map.put(t1, v1);
        map.put(t2, v2);
        map.put(t3, v3);
    }

    private BiomeEssenceInfluence() {}

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Returns the essence affinity multiplier for a given biome and essence type.
     * Returns 1.0 if no special affinity exists.
     */
    public static float getAffinity(ResourceKey<Biome> biome, EssenceType type) {
        Map<EssenceType, Float> affinities = BIOME_AFFINITIES.get(biome);
        if (affinities == null) return 1.0F;
        return affinities.getOrDefault(type, 1.0F);
    }

    /**
     * Returns the essence affinity multiplier for the biome at the given position.
     */
    public static float getAffinity(Level level, BlockPos pos, EssenceType type) {
        var biome = level.getBiome(pos);
        return getAffinity(biome.unwrapKey().orElse(null), type);
    }

    /**
     * Returns all biome affinity multipliers for a given biome.
     */
    public static Map<EssenceType, Float> getAllAffinities(ResourceKey<Biome> biome) {
        Map<EssenceType, Float> bioAff = BIOME_AFFINITIES.get(biome);
        if (bioAff == null) return DEFAULT_AFFINITIES;

        Map<EssenceType, Float> result = new EnumMap<>(DEFAULT_AFFINITIES);
        result.putAll(bioAff);
        return result;
    }

    /** Cached default all-1.0F map to avoid repeated allocation. */
    private static final Map<EssenceType, Float> DEFAULT_AFFINITIES;

    static {
        var defaults = new EnumMap<EssenceType, Float>(EssenceType.class);
        for (EssenceType type : EssenceType.values()) {
            defaults.put(type, 1.0F);
        }
        DEFAULT_AFFINITIES = java.util.Collections.unmodifiableMap(defaults);
    }

    // ─── Player tick: notify about biome essence aura ──────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 600 != 0) return; // every 30 seconds

        var biomeKey = player.level().getBiome(player.blockPosition()).unwrapKey().orElse(null);
        if (biomeKey == null) return;

        Map<EssenceType, Float> affinities = BIOME_AFFINITIES.get(biomeKey);
        if (affinities == null) return;

        // Notify player of dominant biome essence (action bar)
        EssenceType dominant = null;
        float maxAffinity = 1.0F;
        for (Map.Entry<EssenceType, Float> entry : affinities.entrySet()) {
            if (entry.getValue() > maxAffinity) {
                maxAffinity = entry.getValue();
                dominant = entry.getKey();
            }
        }

        if (dominant != null && maxAffinity >= 1.3F) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable(
                            "message.essentialism.biome_affinity",
                            net.minecraft.network.chat.Component.translatable(dominant.translationKey()).withStyle(dominant.color()),
                            net.minecraft.network.chat.Component.literal(String.format("%.0f%%", (maxAffinity - 1.0F) * 100))
                    )
            );
        }
    }
}
