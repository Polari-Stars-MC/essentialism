package com.essentialism.content.block.entity;

import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.menu.SimpleAnalyzerMenu;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SimpleAnalyzerBlockEntity extends BaseBlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_COUNT = 1;
    public static final int DATA_COUNT = 11;

    private static final int PROCESSING_TIME_BASE = 40;
    private static final int PROCESSING_TIME_FAST = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    protected int processingProgress = 0;
    protected int processingTotal = PROCESSING_TIME_BASE;
    private final int[] essenceDisplay = new int[9];
    private boolean analysisDone = false;
    @Nullable
    private EssenceProfile cachedProfile;

    public SimpleAnalyzerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 9) return essenceDisplay[index];
            return switch (index) {
                case 9 -> processingProgress;
                case 10 -> processingTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index < 9) essenceDisplay[index] = value;
            else if (index == 9) processingProgress = value;
            else if (index == 10) processingTotal = value;
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ItemStack getInputStack() {
        return items.get(SLOT_INPUT);
    }

    public void setInputStack(ItemStack stack) {
        items.set(SLOT_INPUT, stack);
    }

    public boolean hasInput() {
        return !getInputStack().isEmpty();
    }

    public boolean isAnalysisDone() {
        return analysisDone;
    }

    @Nullable
    public EssenceProfile getCachedProfile() {
        return cachedProfile;
    }

    public int[] getEssenceDisplay() {
        return essenceDisplay;
    }

    private int computeProcessingTime() {
        if (this.level == null) return PROCESSING_TIME_BASE;
        int signal = this.level.getBestNeighborSignal(this.worldPosition);
        if (signal <= 0) return PROCESSING_TIME_BASE;
        return PROCESSING_TIME_BASE - (signal * (PROCESSING_TIME_BASE - PROCESSING_TIME_FAST) / 15);
    }

    private void analyzeInput() {
        ItemStack input = getInputStack();
        if (input.isEmpty()) {
            resetAnalysis();
            return;
        }

        EssenceProfile profile = EssenceProfiles.resolveBlockFromItem(input);
        this.cachedProfile = profile;
        this.analysisDone = true;

        if (profile != null) {
            EssenceType[] types = EssenceType.values();
            for (int i = 0; i < 9; i++) {
                essenceDisplay[i] = Math.round(profile.get(types[i]));
            }
        } else {
            for (int i = 0; i < 9; i++) {
                essenceDisplay[i] = 0;
            }
        }
    }

    private void resetAnalysis() {
        this.analysisDone = false;
        this.cachedProfile = null;
        for (int i = 0; i < 9; i++) {
            essenceDisplay[i] = 0;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SimpleAnalyzerBlockEntity be) {
        if (be.hasInput() && !be.analysisDone) {
            int required = be.computeProcessingTime();
            be.processingTotal = required;
            be.processingProgress++;

            if (be.processingProgress >= required) {
                be.analyzeInput();
            }
            be.setChanged();
            be.syncToClient();
        } else if (!be.hasInput() && (be.analysisDone || be.processingProgress > 0)) {
            be.processingProgress = 0;
            be.resetAnalysis();
            be.setChanged();
            be.syncToClient();
        }
    }

    public void onInputChanged() {
        this.processingProgress = 0;
        this.analysisDone = false;
        this.cachedProfile = null;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.essentialism.simple_analyzer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SimpleAnalyzerMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ProcessingProgress", processingProgress);
        output.putInt("ProcessingTotal", processingTotal);
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
        output.putInt("ProcessingTotal", processingTotal);
        output.putBoolean("AnalysisDone", analysisDone);
        output.putIntArray("EssenceDisplay", essenceDisplay);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        processingProgress = input.getIntOr("ProcessingProgress", 0);
        processingTotal = input.getIntOr("ProcessingTotal", PROCESSING_TIME_BASE);
        analysisDone = input.getBooleanOr("AnalysisDone", false);
        int[] loaded = input.getIntArray("EssenceDisplay").orElse(new int[9]);
        System.arraycopy(loaded, 0, essenceDisplay, 0, Math.min(loaded.length, 9));
    }
}
