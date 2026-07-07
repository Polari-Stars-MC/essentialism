package com.essentialism.content.block.entity;

import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.mechanic.EssenceResonance;
import com.essentialism.content.menu.AdvancedSpectrometerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Advanced Spectrometer — batch scans up to 9 items, shows essence charts,
 * predicts chunk resonance thresholds, and identifies rare variants.
 */
public class AdvancedSpectrometerBlockEntity extends BaseBlockEntity implements MenuProvider {

    public static final int SLOT_COUNT = 9;
    public static final int DATA_COUNT = 81; // 9 slots × 9 essence types

    private static final int PROCESSING_TIME = 20; // 1 second per batch

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    protected int processingProgress = 0;
    protected boolean analysisDone = false;
    private final int[] essenceDisplay = new int[81]; // flattened: [slot*9 + essenceIndex]

    public AdvancedSpectrometerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 81) return essenceDisplay[index];
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index < 81) essenceDisplay[index] = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    public boolean hasItems() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) return true;
        }
        return false;
    }

    public boolean isAnalysisDone() {
        return analysisDone;
    }

    public int[] getEssenceDisplay() {
        return essenceDisplay;
    }

    /**
     * Returns the chunk essence resonance data for the block's position.
     */
    public float[] getChunkResonance() {
        if (this.level == null) return new float[9];
        ChunkPos cp = new ChunkPos(this.worldPosition.getX() >> 4, this.worldPosition.getZ() >> 4);
        float[] result = new float[9];
        int i = 0;
        for (EssenceType type : EssenceType.values()) {
            result[i] = EssenceResonance.getChunkEssence(cp, type);
            i++;
        }
        return result;
    }

    public int[] getChunkThresholds() {
        int[] thresholds = new int[9];
        int i = 0;
        for (EssenceType type : EssenceType.values()) {
            thresholds[i] = EssenceResonance.getThreshold(type);
            i++;
        }
        return thresholds;
    }

    private void analyzeAll() {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            ItemStack stack = items.get(slot);
            if (stack.isEmpty()) continue;

            EssenceProfile profile = EssenceProfiles.resolveBlockFromItem(stack);
            if (profile != null) {
                int base = slot * 9;
                EssenceType[] types = EssenceType.values();
                for (int i = 0; i < 9; i++) {
                    essenceDisplay[base + i] = Math.round(profile.get(types[i]));
                }
            }
        }
        this.analysisDone = true;
    }

    private void resetAnalysis() {
        this.analysisDone = false;
        for (int i = 0; i < 81; i++) {
            essenceDisplay[i] = 0;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AdvancedSpectrometerBlockEntity be) {
        if (be.hasItems() && !be.analysisDone) {
            be.processingProgress++;
            if (be.processingProgress >= PROCESSING_TIME) {
                be.analyzeAll();
            }
            be.setChanged();
            be.syncToClient();
        } else if (!be.hasItems() && (be.analysisDone || be.processingProgress > 0)) {
            be.processingProgress = 0;
            be.resetAnalysis();
            be.setChanged();
            be.syncToClient();
        }
    }

    public void onSlotsChanged() {
        this.processingProgress = 0;
        this.analysisDone = false;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.essentialism.advanced_spectrometer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AdvancedSpectrometerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ProcessingProgress", processingProgress);
        output.putBoolean("AnalysisDone", analysisDone);
        output.putIntArray("EssenceDisplay", essenceDisplay);
        tag.merge(output.buildResult());
        return tag;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ProcessingProgress", processingProgress);
        output.putBoolean("AnalysisDone", analysisDone);
        output.putIntArray("EssenceDisplay", essenceDisplay);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        processingProgress = input.getIntOr("ProcessingProgress", 0);
        analysisDone = input.getBooleanOr("AnalysisDone", false);
        int[] loaded = input.getIntArray("EssenceDisplay").orElse(new int[81]);
        System.arraycopy(loaded, 0, essenceDisplay, 0, Math.min(loaded.length, 81));
    }
}
