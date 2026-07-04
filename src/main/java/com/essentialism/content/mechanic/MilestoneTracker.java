package com.essentialism.content.mechanic;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.EssenceType;
import com.essentialism.init.EItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

/**
 * Tracks player milestones across the five game stages defined in the design document.
 * <p>
 * Stage 1 — Discovery: scan 50 different blocks
 * Stage 2 — Refinement: obtain first 60+ concentration essence solution
 * Stage 3 — Reconstruction: craft first essence-reconstructed item
 * Stage 4 — Mastery: trigger all 9 chunk resonance effects
 * Stage 5 — Paradox: obtain a paradox item
 */
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class MilestoneTracker {

    /** Player UUID → milestone progress */
    private static final Map<UUID, MilestoneProgress> PROGRESS = new HashMap<>();

    private MilestoneTracker() {}

    // ─── Public API ────────────────────────────────────────────────────

    public static MilestoneProgress get(Player player) {
        return PROGRESS.computeIfAbsent(player.getUUID(), k -> new MilestoneProgress());
    }

    /**
     * Call when player scans a new block type with Analyzer's Lens.
     */
    public static void onBlockScanned(Player player) {
        MilestoneProgress mp = get(player);
        if (mp.blocksScanned < 50) {
            mp.blocksScanned++;
            if (mp.blocksScanned == 50 && player instanceof ServerPlayer sp) {
                announce(sp, "milestone.essentialism.stage1",
                        "发现阶段完成：已扫描 50 种方块的本质签名。",
                        ChatFormatting.AQUA);
            }
        }
    }

    /**
     * Call when player obtains a high-concentration essence solution.
     */
    public static void onEssenceObtained(Player player, float concentration) {
        MilestoneProgress mp = get(player);
        if (!mp.firstHighEssence && concentration >= 60) {
            mp.firstHighEssence = true;
            if (player instanceof ServerPlayer sp) {
                announce(sp, "milestone.essentialism.stage2",
                        "精炼阶段完成：获得首瓶 60+ 浓度本质溶液。",
                        ChatFormatting.GREEN);
            }
        }
    }

    /**
     * Call when player successfully reconstructs an item.
     */
    public static void onItemReconstructed(Player player) {
        MilestoneProgress mp = get(player);
        if (!mp.firstReconstruction) {
            mp.firstReconstruction = true;
            if (player instanceof ServerPlayer sp) {
                announce(sp, "milestone.essentialism.stage3",
                        "重构阶段完成：成功制作首件本质重构物品。",
                        ChatFormatting.GOLD);
            }
        }
    }

    /**
     * Call when player triggers a chunk resonance effect.
     */
    public static void onResonanceTriggered(Player player, EssenceType type) {
        MilestoneProgress mp = get(player);
        if (mp.triggeredResonances.add(type)) {
            if (mp.triggeredResonances.size() == 9 && player instanceof ServerPlayer sp) {
                announce(sp, "milestone.essentialism.stage4",
                        "大师阶段完成：触发了全部 9 种区块本质共振。",
                        ChatFormatting.LIGHT_PURPLE);
            }
        }
    }

    /**
     * Call when player obtains a paradox item.
     */
    public static void onParadoxObtained(Player player) {
        MilestoneProgress mp = get(player);
        if (!mp.hasParadox) {
            mp.hasParadox = true;
            if (player instanceof ServerPlayer sp) {
                announce(sp, "milestone.essentialism.stage5",
                        "悖论阶段：获得了悖论物品。你已触及世界的底层法则。",
                        ChatFormatting.DARK_PURPLE);
            }
        }
    }

    // ─── Event: check paradox items in inventory ──────────────────────

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().getGameTime() % 100 != 0) return; // check every 5 seconds

        MilestoneProgress mp = get(player);
        if (mp.hasParadox) return; // already achieved

        // Check inventory for paradox items
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof com.essentialism.content.item.ParadoxItem) {
                onParadoxObtained(player);
                return;
            }
        }

        // Check for essence solutions with high concentration
        if (!mp.firstHighEssence) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (stack.getItem() instanceof com.essentialism.content.item.EssenceSolutionItem solution) {
                    if (solution.getConcentration() >= 60) {
                        onEssenceObtained(player, solution.getConcentration());
                        break;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            // Ensure progress is loaded
            get(sp);
        }
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private static void announce(ServerPlayer player, String key, String message, ChatFormatting color) {
        player.sendSystemMessage(Component.literal("§l✦ ").append(
                Component.literal(message).withStyle(color)
        ));
        player.sendSystemMessage(Component.empty());
    }

    // ─── Data class ────────────────────────────────────────────────────

    public static class MilestoneProgress {
        public int blocksScanned = 0;
        public boolean firstHighEssence = false;
        public boolean firstReconstruction = false;
        public final Set<EssenceType> triggeredResonances = EnumSet.noneOf(EssenceType.class);
        public boolean hasParadox = false;

        public String stageName() {
            if (hasParadox) return "悖论阶段";
            if (triggeredResonances.size() >= 9) return "大师阶段";
            if (firstReconstruction) return "重构阶段";
            if (firstHighEssence) return "精炼阶段";
            if (blocksScanned >= 50) return "发现阶段";
            return "初始阶段";
        }
    }
}
