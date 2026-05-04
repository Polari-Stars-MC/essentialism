package com.essentialism.content.item;

import com.essentialism.Essentialism;
import com.essentialism.content.essence.CombinedEssenceType;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.init.EItems;
import com.essentialism.network.AnalyzerLensSigilS2CPacket;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = Essentialism.MOD_ID)
public class AnalyzerLensItem extends Item {
    public AnalyzerLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            inspectBlock(player, context.getLevel(), context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(
            Level level,
            net.minecraft.world.entity.player.Player player,
            InteractionHand hand
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        inspectBlock(serverPlayer, level, hitResult.getBlockPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.SUCCESS;
        }

        inspectBlock(player, context.getLevel(), context.getClickedPos());
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            net.minecraft.world.entity.player.Player player,
            LivingEntity interactionTarget,
            InteractionHand usedHand
    ) {
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, EquipmentSlot slot) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.getMainHandItem() != stack && player.getOffhandItem() != stack) {
            return;
        }
        HitResult focus = findFocus(player);
        if (focus == null || isDisallowedPlayerTarget(player, focus)) {
            return;
        }
        Vec3 location = focus.getLocation();
        double groundY = resolveGroundY(level, location);
        if (Double.isNaN(groundY)) {
            return;
        }
        if (!Essentialism.ESSENCE_CONFIG.isPlayerEssence)
            return;
        PacketDistributor.sendToPlayersNear(
                level,
                player,
                location.x,
                groundY,
                location.z,
                160.0D,
                new AnalyzerLensSigilS2CPacket(location.x, groundY, location.z, 1.85F, 0xC040F0FF, 2)
        );
    }

    public static double resolveGroundY(Level level, Vec3 focus) {
        BlockPos cursor = BlockPos.containing(focus.x, focus.y, focus.z);
        for (int i = 0; i < 16; i++) {
            BlockState state = level.getBlockState(cursor);
            if (!state.isAir() && !state.getShape(level, cursor).isEmpty()) {
                return cursor.getY() + state.getShape(level, cursor).max(net.minecraft.core.Direction.Axis.Y);
            }
            cursor = cursor.below();
        }

        return Double.NaN;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, net.minecraft.core.BlockPos pos,
            net.minecraft.world.entity.player.Player player) {
        return true;
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof LivingEntity livingEntity)) {
            return;
        }
        if (!event.getItemStack().is(EItems.ANALYZERS_LENS.get())) {
            return;
        }
        if (livingEntity instanceof Player && !Essentialism.ESSENCE_CONFIG.isPlayerEssence) {
            return;
        }

        EssenceProfile profile = EssenceProfiles.get(livingEntity);
        if (profile == null || !Essentialism.ESSENCE_CONFIG.isPlayerEssence) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            inspectProfile(serverPlayer, livingEntity.getDisplayName(), profile);
        }

        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static void inspectBlock(ServerPlayer player, Level level, net.minecraft.core.BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        EssenceProfile profile = EssenceProfiles.get(level, pos);
        inspectProfile(player, state.getBlock().getName(), profile);
    }

    private static void inspectProfile(ServerPlayer player, Component targetName, EssenceProfile profile) {
        if (profile == null) {
            player.sendSystemMessage(Component.translatable(
                    "message.essentialism.analyzers_lens.unknown",
                    targetName
            ).withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        player.sendSystemMessage(Component.translatable(
                "message.essentialism.analyzers_lens.header",
                targetName
        ).withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(profile.summaryComponent());

        if (player.isShiftKeyDown()) {
            player.sendSystemMessage(Component.translatable("message.essentialism.analyzers_lens.detail")
                    .withStyle(ChatFormatting.GRAY));
            player.sendSystemMessage(profile.detailedComponent());
            List<Component> dominant = profile.dominantComponents();
            for (Component line : dominant) {
                player.sendSystemMessage(line.copy().withStyle(ChatFormatting.DARK_AQUA));
            }
            List<Component> combined = CombinedEssenceType.detect(profile);
            if (!combined.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.essentialism.analyzers_lens.combined")
                        .withStyle(ChatFormatting.GOLD));
                for (Component line : combined) {
                    player.sendSystemMessage(line.copy().withStyle(ChatFormatting.GOLD));
                }
            }
        } else {
            player.sendSystemMessage(Component.translatable("message.essentialism.analyzers_lens.hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static boolean isDisallowedPlayerTarget(ServerPlayer source, HitResult focus) {
        if (Essentialism.ESSENCE_CONFIG.isPlayerEssence || !(focus instanceof EntityHitResult entityHit)) {
            return false;
        }
        return entityHit.getEntity() instanceof Player target && target != source;
    }

    private static HitResult findFocus(ServerPlayer player) {
        Vec3 eyePosition = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1.0F);
        double blockRange = player.blockInteractionRange();
        Vec3 blockEnd = eyePosition.add(viewVector.scale(blockRange));
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePosition,
                blockEnd,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));
        double blockDistanceSqr = blockHit.getType() == HitResult.Type.MISS
                ? Double.POSITIVE_INFINITY
                : eyePosition.distanceToSqr(blockHit.getLocation());

        double entityRange = player.entityInteractionRange();
        Vec3 entityEnd = eyePosition.add(viewVector.scale(entityRange));
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                player.level(),
                player,
                eyePosition,
                entityEnd,
                player.getBoundingBox().expandTowards(viewVector.scale(entityRange)).inflate(1.0D),
                candidate -> candidate instanceof LivingEntity && candidate.isPickable(),
                (float) entityRange
        );
        double entityDistanceSqr = entityHit == null
                ? Double.POSITIVE_INFINITY
                : eyePosition.distanceToSqr(entityHit.getLocation());
        if (entityDistanceSqr < blockDistanceSqr) {
            return entityHit;
        }
        return blockHit.getType() == HitResult.Type.MISS ? null : blockHit;
    }
}
