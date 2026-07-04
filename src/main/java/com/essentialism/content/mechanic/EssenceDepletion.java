package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.init.EItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks extraction counts per block position and applies depletion
 * when the same block is over-extracted.
 * <p>
 * Based on the design document's "Essence Depletion (Over-extraction Penalty)".
 * After 5 extractions, the block enters a depleted state:
 * - Crack particles appear
 * - Essence output drops to 10%
 * - Recovers after ~3 Minecraft days (72000 ticks)
 * - Continuing extraction during depletion has a 10% chance of permanent destruction
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class EssenceDepletion {

    /** Maximum extractions before depletion kicks in */
    private static final int DEPLETION_THRESHOLD = 5;

    /** Recovery time in ticks (~3 Minecraft days = 72000) */
    private static final long RECOVERY_TICKS = 72000;

    /** Probability (0-1) of permanent destruction when extracting from depleted block */
    private static final float DESTROY_CHANCE = 0.10F;

    /** Particle display interval in ticks */
    private static final int PARTICLE_INTERVAL = 20;

    /**
     * BlockPos → (extractionCount, depletionStartTick).
     * null depletionStartTick means not yet depleted.
     */
    private static final Map<BlockPos, DepletionEntry> DEPLETION_MAP = new ConcurrentHashMap<>();

    /** Tracks when we last emitted particles */
    private static long lastParticleTick = 0;

    private EssenceDepletion() {}

    // ─── Event handlers ────────────────────────────────────────────────

    /**
     * When a block is broken while the player holds an Analyzer's Lens,
     * increment the extraction counter for that position.
     */
    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;
        // Only track when the player uses the lens (extraction tool)
        if (!event.getPlayer().getMainHandItem().is(EItems.ANALYZERS_LENS.get())) return;

        BlockPos pos = event.getPos();
        DepletionEntry entry = DEPLETION_MAP.computeIfAbsent(pos.immutable(), k -> new DepletionEntry());

        // Check if recovered
        if (entry.depleted && entry.depletionStartTick > 0) {
            long elapsed = event.getLevel().getGameTime() - entry.depletionStartTick;
            if (elapsed >= RECOVERY_TICKS) {
                // Recovered!
                entry.count = 0;
                entry.depleted = false;
                entry.depletionStartTick = 0;
            }
        }

        if (entry.depleted) {
            // Depleted: 10% chance of permanent destruction
            if (Math.random() < DESTROY_CHANCE) {
                DEPLETION_MAP.remove(pos);
                // Replace with air (permanent disappearance)
                event.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
            // Still depleted: output at 10% — handled by EssenceProfiles
            return;
        }

        entry.count++;
        if (entry.count >= DEPLETION_THRESHOLD) {
            entry.depleted = true;
            entry.depletionStartTick = event.getLevel().getGameTime();
        }
    }

    /**
     * Periodic particle emission for depleted blocks.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;
        Level level = event.getLevel();
        long gameTime = level.getGameTime();

        if (gameTime - lastParticleTick < PARTICLE_INTERVAL) return;
        lastParticleTick = gameTime;

        Iterator<Map.Entry<BlockPos, DepletionEntry>> iter = DEPLETION_MAP.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<BlockPos, DepletionEntry> entry = iter.next();
            BlockPos pos = entry.getKey();
            DepletionEntry de = entry.getValue();

            // Clean up recovered entries
            if (de.depleted && de.depletionStartTick > 0) {
                long elapsed = gameTime - de.depletionStartTick;
                if (elapsed >= RECOVERY_TICKS) {
                    iter.remove();
                    continue;
                }
            }

            if (de.depleted && level instanceof ServerLevel serverLevel) {
                // Emit crack particles
                double x = pos.getX() + 0.5;
                double y = pos.getY() + 0.5;
                double z = pos.getZ() + 0.5;
                serverLevel.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,
                        x, y, z,
                        2, // count
                        0.4, 0.4, 0.4, // spread
                        0.02 // speed
                );
            }
        }
    }

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Returns whether a block position is currently depleted.
     */
    public static boolean isDepleted(BlockPos pos) {
        DepletionEntry entry = DEPLETION_MAP.get(pos);
        return entry != null && entry.depleted;
    }

    /**
     * Returns the extraction count for a block position.
     */
    public static int getExtractionCount(BlockPos pos) {
        DepletionEntry entry = DEPLETION_MAP.get(pos);
        return entry == null ? 0 : entry.count;
    }

    /**
     * Returns the depletion multiplier for essence output.
     * Normal: 1.0, Depleted: 0.1 (10%).
     */
    public static float getOutputMultiplier(BlockPos pos) {
        return isDepleted(pos) ? 0.1F : 1.0F;
    }

    // ─── Internal ──────────────────────────────────────────────────────

    private static class DepletionEntry {
        int count = 0;
        boolean depleted = false;
        long depletionStartTick = 0;
    }
}
