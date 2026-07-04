package com.essentialism.content.block;

import com.essentialism.content.block.entity.EssenceResonanceMatrixBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Essence Resonance Matrix — central controller block of the 3×3×3 multiblock.
 * <p>
 * Construction: 3×3×3 amethyst_block frame with a sculk_catalyst at the center.
 * When formed, monitors a 32-block radius for essence fluctuations,
 * outputs redstone when thresholds are exceeded, and can trigger area effects.
 */
public class EssenceResonanceMatrixBlock extends BaseEntityBlock {

    public EssenceResonanceMatrixBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(EssenceResonanceMatrixBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EssenceResonanceMatrixBlockEntity(null, pos, state);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<EssenceResonanceMatrixBlockEntity>) EssenceResonanceMatrixBlockEntity::serverTick;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof EssenceResonanceMatrixBlockEntity matrix) {
            if (matrix.isFormed()) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("message.essentialism.matrix.status",
                                matrix.getMonitoredEssenceType().translationKey(),
                                matrix.getThreshold(),
                                matrix.getCurrentEssenceValue())
                );
            } else {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.translatable("message.essentialism.matrix.not_formed")
                );
            }
        }
        return InteractionResult.CONSUME;
    }

    /**
     * Checks if a 3×3×3 amethyst frame with sculk catalyst center exists.
     * Center block is this block's position. The frame must be exactly 3×3×3 of amethyst_block
     * with the center being sculk_catalyst and this block replacing one of the frame corners.
     *
     * Pattern (any axis orientation):
     * Layer 0:  3×3 amethyst_block (with this block at one corner or edge)
     * Layer 1:  3×3 amethyst_block ring, center = sculk_catalyst
     * Layer 2:  3×3 amethyst_block
     *
     * For simplicity, we check if the 3×3×3 region centered on this block
     * contains the required blocks.
     */
    public static boolean validateMultiblock(Level level, BlockPos controllerPos) {
        // The controller is placed as one of the blocks in the frame.
        // We scan a 5×5×5 area around the controller to find a valid 3×3×3 structure.
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    // candidate center = controllerPos + (dx, dy, dz)
                    BlockPos center = controllerPos.offset(dx, dy, dz);
                    if (checkStructure(level, center)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkStructure(Level level, BlockPos center) {
        // Center must contain the controller block or sculk_catalyst
        // Actually: center should be sculk_catalyst, and the 3×3×3 frame must be amethyst_block
        // The controller block replaces ONE of the amethyst_block positions

        int amethystCount = 0;
        boolean hasCatalyst = false;
        boolean hasController = false;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (x == 0 && y == 0 && z == 0) {
                        // Center: must be sculk_catalyst
                        if (state.is(Blocks.SCULK_CATALYST)) {
                            hasCatalyst = true;
                        }
                        continue;
                    }

                    // Frame positions: must be amethyst_block or the controller block
                    if (state.is(Blocks.AMETHYST_BLOCK)) {
                        amethystCount++;
                    } else if (state.getBlock() instanceof EssenceResonanceMatrixBlock) {
                        amethystCount++;
                        hasController = true;
                    }
                }
            }
        }

        // 3×3×3 = 27 blocks, center = 1 catalyst, frame = 26 amethyst (or 25 + 1 controller)
        return hasCatalyst && amethystCount >= 25 && hasController;
    }
}
