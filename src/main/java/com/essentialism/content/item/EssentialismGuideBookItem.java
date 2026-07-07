package com.essentialism.content.item;

import com.essentialism.content.essence.EssenceInteraction;
import com.essentialism.content.essence.EssenceLevel;
import com.essentialism.content.essence.EssenceType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * 《本质主义》— in-game guide book opened via right-click.
 */
public class EssentialismGuideBookItem extends Item {

    private static final int BOOK_COOLDOWN_TICKS = 40;

    public EssentialismGuideBookItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        player.getCooldowns().addCooldown(context.getItemInHand(), BOOK_COOLDOWN_TICKS);
        showGuide(player);
        return InteractionResult.SUCCESS;
    }

    private void showGuide(Player player) {
        sendHeader(player, "《本质主义》第一卷 · 序言");
        player.sendSystemMessage(Component.literal(
                "\"世界从未向你隐藏，你只是从未学会观看。\"")
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 第一章：九大本质 ──");
        for (EssenceType type : EssenceType.values()) {
            player.sendSystemMessage(Component.literal("  ◆ ").append(
                    Component.translatable(type.translationKey()).withStyle(type.color()))
                    .append(Component.literal(" — " + type.sigil()).withStyle(ChatFormatting.DARK_GRAY)));
        }
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 第二章：浓度等级 ──");
        for (EssenceLevel tier : EssenceLevel.values()) {
            player.sendSystemMessage(Component.literal(
                    String.format("  %-4s | %3d-%-3d | %s",
                            tier.chineseName(), tier.minValue(),
                            tier.maxValue() == Integer.MAX_VALUE ? 999 : tier.maxValue(),
                            tier.particleDescription()))
                    .withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 第三章：本质相互作用 ──");
        for (EssenceType a : EssenceType.values()) {
            for (EssenceType b : EssenceType.values()) {
                if (a.ordinal() >= b.ordinal()) continue;
                EssenceInteraction interaction = EssenceInteraction.between(a, b);
                if (interaction == null) continue;
                player.sendSystemMessage(Component.literal("  ").append(
                        Component.translatable(a.translationKey()).withStyle(a.color()))
                        .append(Component.literal(" ←→ ").withStyle(ChatFormatting.WHITE))
                        .append(Component.translatable(b.translationKey()).withStyle(b.color()))
                        .append(Component.literal("  " + interaction.chineseName())
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
        }
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 第四章：游戏阶段 ──");
        player.sendSystemMessage(Component.literal("  初始阶段：合成解析者透镜，扫描 50 种方块。").withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(Component.literal("  进阶阶段：合成简易解析器与离心机，获得 60+ 浓度溶液。").withStyle(ChatFormatting.GREEN));
        player.sendSystemMessage(Component.literal("  深化阶段：合成光谱仪与编纂桌，制作首件重构物品。").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.literal("  巅峰阶段：构建本质共鸣矩阵，触发全部 9 种共振。").withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.literal("  终局挑战：合成悖论物品，触碰世界的底层法则。").withStyle(ChatFormatting.DARK_PURPLE));
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 第五章：核心装置 ──");
        player.sendSystemMessage(Component.literal("  解析者透镜 — 紫水晶碎片+铜锭+玻璃板").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("  简易解析器 — 透镜+铁锭+红石比较器").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("  灵魂离心机 — 下界合金锭+幽匿催发体+灵魂沙").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("  进阶光谱仪 — 简易解析器+末影珍珠+下界石英块").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("  符文矩阵编纂桌 — 下界合金块+附魔台+紫水晶块").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal("  本质共鸣矩阵 — 3×3×3紫水晶框架+中央幽匿催发体").withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.empty());

        sendHeader(player, "── 终章：哲学注脚 ──");
        for (EssenceType type : EssenceType.values()) {
            player.sendSystemMessage(Component.literal("  ").append(
                    Component.translatable(type.translationKey()).withStyle(type.color()))
                    .append(Component.literal("：").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(type.philosophyNote()).withStyle(ChatFormatting.DARK_GRAY)));
        }
        player.sendSystemMessage(Component.empty());
        sendHeader(player, "——《本质主义》，全书完 ——");
    }

    private static void sendHeader(Player player, String title) {
        player.sendSystemMessage(Component.literal(title)
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }
}
