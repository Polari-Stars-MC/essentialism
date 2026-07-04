package com.essentialism.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;

/**
 * Reconstruction items — essence-crafted artifacts.
 * <p>
 * Void Blade: Solidity 50 + Shadow 30. Deletes entities.
 * Time Bucket: Motion 30 + Spacetime 40. Slows nearby entities.
 * Memory Crystal: Mind 70 + Resonance 15. Grants experience.
 * Mourning Grass: Life 60 + Decay 40 + Mind 20. Spreads grass to dirt.
 */
public class ReconstructedItem extends Item {

    public enum ReconstructedType {
        VOID_BLADE("void_blade", "虚空之刃"),
        TIME_BUCKET("time_bucket", "时间之桶"),
        MEMORY_CRYSTAL("memory_crystal", "记忆结晶"),
        MOURNING_GRASS("mourning_grass", "哀悼草");

        private final String id;
        private final String chineseName;

        ReconstructedType(String id, String chineseName) {
            this.id = id;
            this.chineseName = chineseName;
        }

        public String id() { return id; }
        public String chineseName() { return chineseName; }
    }

    private final ReconstructedType type;

    public ReconstructedItem(Properties properties, ReconstructedType type) {
        super(properties);
        this.type = type;
    }

    public ReconstructedType getReconstructedType() { return type; }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("「" + type.chineseName + "」").withStyle(ChatFormatting.DARK_AQUA);
    }

    // ─── Void Blade: one-hit kill ─────────────────────────────────────

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (type != ReconstructedType.VOID_BLADE) return false;
        if (!(target instanceof LivingEntity living)) return false;
        if (player.level().isClientSide()) return true;

        living.hurt(living.damageSources().outOfBorder(), Float.MAX_VALUE);
        if (player.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.PORTAL,
                    living.getX(), living.getY() + 1, living.getZ(),
                    50, 1.0, 1.5, 1.0, 0.2);
        }
        player.sendSystemMessage(Component.translatable("message.essentialism.void_blade.strike")
                .withStyle(ChatFormatting.DARK_PURPLE));
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return true;
    }

    // ─── Right-click behaviors ─────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        ItemStack stack = context.getItemInHand();
        if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;

        switch (type) {
            case TIME_BUCKET -> {
                var nearby = context.getLevel().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(5.0), e -> !(e instanceof Player));
                for (LivingEntity entity : nearby) {
                    entity.addEffect(new MobEffectInstance(
                            MobEffects.SLOWNESS, 200, 3, false, true, true));
                }
                player.sendSystemMessage(Component.translatable("message.essentialism.time_bucket.activate")
                        .withStyle(ChatFormatting.BLUE));
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            case MEMORY_CRYSTAL -> {
                player.giveExperiencePoints(100);
                player.sendSystemMessage(Component.translatable("message.essentialism.memory_crystal.use")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
            case MOURNING_GRASS -> {
                BlockPos pos = player.blockPosition();
                int spread = 0;
                for (int dx = -2; dx <= 2 && spread < 5; dx++) {
                    for (int dz = -2; dz <= 2 && spread < 5; dz++) {
                        BlockPos target = pos.offset(dx, 0, dz);
                        if (context.getLevel().getBlockState(target).is(Blocks.DIRT)) {
                            context.getLevel().setBlock(target, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                            spread++;
                        }
                    }
                }
                if (!player.isCreative()) stack.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
