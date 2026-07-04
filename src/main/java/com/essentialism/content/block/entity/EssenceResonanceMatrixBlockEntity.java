package com.essentialism.content.block.entity;

import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.mechanic.EssenceResonance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * BlockEntity for the Essence Resonance Matrix.
 * <p>
 * Continuously monitors essence levels in a 32-block radius.
 * When the configured essence type exceeds the threshold,
 * emits a redstone signal and triggers area alerts.
 */
public class EssenceResonanceMatrixBlockEntity extends BlockEntity {

    private static final int SCAN_RADIUS = 32;
    private static final int SCAN_INTERVAL = 100; // every 5 seconds

    private EssenceType monitoredType = EssenceType.SOLIDITY;
    private int threshold = 5000;
    private boolean formed = false;
    private float currentEssenceValue = 0;
    private int redstoneOutput = 0;

    public EssenceResonanceMatrixBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public boolean isFormed() {
        return formed;
    }

    public EssenceType getMonitoredEssenceType() {
        return monitoredType;
    }

    public int getThreshold() {
        return threshold;
    }

    public float getCurrentEssenceValue() {
        return currentEssenceValue;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, EssenceResonanceMatrixBlockEntity be) {
        if (level.getGameTime() % SCAN_INTERVAL != 0) return;

        // Validate multiblock
        boolean wasFormed = be.formed;
        be.formed = com.essentialism.content.block.EssenceResonanceMatrixBlock.validateMultiblock(level, pos);

        if (be.formed) {
            if (!wasFormed) {
                // Just formed!
                be.syncToClient();
            }
            be.scanEssence();
            be.updateRedstone(level, pos);
        } else if (wasFormed) {
            // Structure broken
            be.redstoneOutput = 0;
            be.syncToClient();
        }
    }

    private void scanEssence() {
        ChunkPos chunkPos = new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4);
        this.currentEssenceValue = EssenceResonance.getChunkEssence(chunkPos, this.monitoredType);

        // Also sample neighboring chunks within radius
        int radiusChunks = SCAN_RADIUS / 16 + 1;
        float total = this.currentEssenceValue;
        int count = 1;
        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                if (dx == 0 && dz == 0) continue;
                ChunkPos neighbor = new ChunkPos(chunkPos.getMinBlockX() >> 4 + dx, chunkPos.getMinBlockZ() >> 4 + dz);
                total += EssenceResonance.getChunkEssence(neighbor, this.monitoredType);
                count++;
            }
        }
        this.currentEssenceValue = total / count;
    }

    private void updateRedstone(Level level, BlockPos pos) {
        int newOutput = 0;
        if (this.currentEssenceValue >= this.threshold) {
            // Scale redstone output: 1-15 based on how much over threshold
            float ratio = this.currentEssenceValue / this.threshold;
            newOutput = Math.min(15, (int) (ratio * 15));
        }

        if (newOutput != this.redstoneOutput) {
            this.redstoneOutput = newOutput;
            level.updateNeighborsAt(pos, this.getBlockState().getBlock());
            this.setChanged();
            this.syncToClient();
        }
    }

    public int getRedstoneOutput() {
        return redstoneOutput;
    }

    // Redstone signal provider
    public int getSignal() {
        return this.redstoneOutput;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("MonitoredType", this.monitoredType.name());
        tag.putInt("Threshold", this.threshold);
        tag.putBoolean("Formed", this.formed);
        tag.putFloat("CurrentValue", this.currentEssenceValue);
        tag.putInt("RedstoneOutput", this.redstoneOutput);
        return tag;
    }

    public void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putString("MonitoredType", this.monitoredType.name());
        output.putInt("Threshold", this.threshold);
        output.putBoolean("Formed", this.formed);
        output.putFloat("CurrentValue", this.currentEssenceValue);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        String typeName = input.getStringOr("MonitoredType", "SOLIDITY");
        try {
            this.monitoredType = EssenceType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            this.monitoredType = EssenceType.SOLIDITY;
        }
        this.threshold = input.getIntOr("Threshold", 5000);
        this.formed = input.getBooleanOr("Formed", false);
        this.currentEssenceValue = input.getFloatOr("CurrentValue", 0);
    }
}
