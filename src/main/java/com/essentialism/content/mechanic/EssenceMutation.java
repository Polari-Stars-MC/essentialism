package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.*;

/**
 * Handles rare essence mutation drops when breaking blocks.
 * <p>
 * Based on the design document's "Essence Mutation (Rare Variants)".
 * When any block is broken, there is a 0.5% chance of dropping a mutated variant.
 * <p>
 * Five mutation types:
 * - PURE: single dominant essence, concentration +50%
 * - RESONANT: resonance doubled, others halved
 * - VOID: high shadow + spacetime, others zeroed
 * - PARADOXICAL: life and decay both extreme
 * - TIME_AMBER: freezes the block state for later restoration
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class EssenceMutation {

    /** Base mutation chance: 0.5% */
    private static final float MUTATION_CHANCE = 0.005F;

    /** The five mutation types */
    public enum MutationType {
        PURE("pure"),
        RESONANT("resonant"),
        VOID("void"),
        PARADOXICAL("paradoxical"),
        TIME_AMBER("time_amber");

        private final String id;

        MutationType(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    private static final Random RANDOM = new Random();

    private EssenceMutation() {}

    // ─── Event handler ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onBlockBreak(BreakBlockEvent event) {
        if (event.getLevel().isClientSide()) return;

        // 0.5% base chance
        if (RANDOM.nextFloat() >= MUTATION_CHANCE) return;

        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Level level = (Level) event.getLevel();

        // Pick a random mutation type
        MutationType mutationType = pickMutationType(state);
        ItemStack mutantStack = createMutantStack(mutationType, state, pos, level);

        if (mutantStack != null && !mutantStack.isEmpty()) {
            // Spawn as extra drop
            ItemEntity itemEntity = new ItemEntity(
                    level,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    mutantStack
            );
            level.addFreshEntity(itemEntity);
        }
    }

    // ─── Mutation type selection ───────────────────────────────────────

    private static MutationType pickMutationType(BlockState state) {
        // Weighted random selection
        float roll = RANDOM.nextFloat();
        if (roll < 0.30F) return MutationType.PURE;
        if (roll < 0.50F) return MutationType.RESONANT;
        if (roll < 0.70F) return MutationType.VOID;
        if (roll < 0.85F) return MutationType.PARADOXICAL;
        return MutationType.TIME_AMBER;
    }

    // ─── Mutant stack creation ─────────────────────────────────────────

    private static ItemStack createMutantStack(
            MutationType mutationType,
            BlockState state,
            BlockPos pos,
            Level level
    ) {
        Block block = state.getBlock();
        Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
        com.essentialism.content.essence.EssenceProfile profile =
                com.essentialism.content.essence.EssenceProfiles.resolveBlockFromItem(block.asItem());

        switch (mutationType) {
            case PURE -> {
                // Single dominant essence, +50% concentration
                if (profile == null) return ItemStack.EMPTY;
                EssenceType dominant = findDominantEssence(profile);
                if (dominant == null) return ItemStack.EMPTY;
                float boosted = profile.get(dominant) * 1.5F;
                return createSolutionItem(dominant, boosted, "pure");
            }
            case RESONANT -> {
                // Resonance doubled, others halved
                if (profile == null) return ItemStack.EMPTY;
                float resonance = profile.resonance() * 2.0F;
                // Also carry halved values of other essences for flavor
                return createSolutionItem(EssenceType.RESONANCE, Math.min(resonance, 100F), "resonant");
            }
            case VOID -> {
                // High shadow + high spacetime
                float shadow = 60F + RANDOM.nextFloat() * 30F;
                float spacetime = 60F + RANDOM.nextFloat() * 30F;
                // Return as the stronger one
                return createSolutionItem(
                        shadow >= spacetime ? EssenceType.SHADOW : EssenceType.SPACETIME,
                        Math.max(shadow, spacetime),
                        "void"
                );
            }
            case PARADOXICAL -> {
                // Life + Decay both extreme
                float life = 70F + RANDOM.nextFloat() * 20F;
                float decay = 70F + RANDOM.nextFloat() * 20F;
                return createSolutionItem(
                        life >= decay ? EssenceType.LIFE : EssenceType.DECAY,
                        Math.max(life, decay),
                        "paradoxical"
                );
            }
            case TIME_AMBER -> {
                // Time Amber: stores the original block ID for later placement
                return createTimeAmber(blockId);
            }
        }
        return ItemStack.EMPTY;
    }

    private static EssenceType findDominantEssence(EssenceProfile profile) {
        EssenceType best = null;
        float bestValue = 0;
        for (EssenceType type : EssenceType.values()) {
            float v = profile.get(type);
            if (v > bestValue) {
                bestValue = v;
                best = type;
            }
        }
        return best;
    }

    private static ItemStack createSolutionItem(EssenceType type, float concentration, String mutationId) {
        var itemEntry = getSolutionForType(type);
        if (itemEntry == null) return ItemStack.EMPTY;

        ItemStack stack = new ItemStack(itemEntry.asItem());
        // TODO: when DataComponent support is added, store concentration and mutationId
        return stack;
    }

    private static ItemStack createTimeAmber(Identifier blockId) {
        // Time Amber: a special item that stores the block it came from
        ItemStack stack = new ItemStack(com.essentialism.init.EItems.SOLIDITY_SOLUTION.asItem());
        // TODO: store original block ID via DataComponent
        return stack;
    }

    private static dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry<?> getSolutionForType(EssenceType type) {
        return type.solutionItem();
    }
}
