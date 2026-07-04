package com.essentialism.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Paradox items — endgame artifacts that combine opposing essences.
 * <p>
 * Eternal Ember: Life 100 + Decay 100. Burns forever.
 * Light Cloak: Light 100 + Shadow 100. Invisibility + glowing footprints.
 * Static Engine: Motion 100 + Spacetime 100. Freezes nearby entities.
 */
public class ParadoxItem extends Item {

    public enum ParadoxType {
        ETERNAL_EMBER("eternal_ember", "永恒之烬"),
        LIGHT_CLOAK("light_cloak", "光明斗篷"),
        STATIC_ENGINE("static_engine", "静止引擎");

        private final String id;
        private final String chineseName;

        ParadoxType(String id, String chineseName) {
            this.id = id;
            this.chineseName = chineseName;
        }

        public String id() { return id; }
        public String chineseName() { return chineseName; }
    }

    private final ParadoxType paradoxType;

    public ParadoxItem(Properties properties, ParadoxType paradoxType) {
        super(properties);
        this.paradoxType = paradoxType;
    }

    public ParadoxType getParadoxType() {
        return paradoxType;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("「" + paradoxType.chineseName + "」")
                .withStyle(ChatFormatting.GOLD);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (paradoxType == ParadoxType.ETERNAL_EMBER) {
            BlockPos pos = player.blockPosition();
            if (level.isEmptyBlock(pos) && !level.isClientSide()) {
                level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }

        if (paradoxType == ParadoxType.STATIC_ENGINE) {
            if (!level.isClientSide()) {
                // Freeze nearby non-player entities
                List<LivingEntity> nearby = level.getEntitiesOfClass(
                        LivingEntity.class,
                        player.getBoundingBox().inflate(8.0),
                        e -> !(e instanceof Player)
                );
                for (LivingEntity entity : nearby) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.SLOWNESS, 100, 100,
                            false, true, true
                    ));
                }
                player.getCooldowns().addCooldown(stack, 200);
                if (!player.isCreative()) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.use(level, player, hand);
    }
}
