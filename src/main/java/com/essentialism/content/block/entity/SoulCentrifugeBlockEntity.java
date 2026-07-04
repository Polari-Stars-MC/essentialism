package com.essentialism.content.block.entity;

import com.essentialism.content.essence.EssenceProfile;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.item.EssenceSolutionItem;
import com.essentialism.content.menu.SoulCentrifugeMenu;
import com.essentialism.init.EItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Soul Centrifuge — separates mixed-essence items into pure essence solutions.
 * <p>
 * 1 input slot + 3 output slots. Processing consumes experience levels.
 * The dominant essences of the input item are extracted into solution bottles.
 */
public class SoulCentrifugeBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_1 = 1;
    public static final int SLOT_OUTPUT_2 = 2;
    public static final int SLOT_OUTPUT_3 = 3;
    public static final int SLOT_COUNT = 4;
    public static final int DATA_COUNT = 3; // progress, total, expCost

    private static final int PROCESSING_TIME_BASE = 200; // 10 seconds

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    protected int processingProgress = 0;
    protected int processingTotal = PROCESSING_TIME_BASE;
    protected int expCost = 5;

    public SoulCentrifugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> processingProgress;
                case 1 -> processingTotal;
                case 2 -> expCost;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> processingProgress = value;
                case 1 -> processingTotal = value;
                case 2 -> expCost = value;
            }
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

    public ItemStack getOutputStack(int slot) {
        return items.get(slot);
    }

    public void setOutputStack(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    public boolean hasInput() {
        return !getInputStack().isEmpty();
    }

    public boolean hasOutputSpace() {
        return getOutputStack(SLOT_OUTPUT_1).isEmpty()
                || getOutputStack(SLOT_OUTPUT_2).isEmpty()
                || getOutputStack(SLOT_OUTPUT_3).isEmpty();
    }

    /**
     * Calculate processing time based on essence complexity.
     * More essences = longer processing.
     */
    private int computeProcessingTime() {
        ItemStack input = getInputStack();
        if (input.isEmpty()) return PROCESSING_TIME_BASE;

        EssenceProfile profile = EssenceProfiles.resolveBlockFromItem(input);
        if (profile == null) return PROCESSING_TIME_BASE;

        int essenceCount = 0;
        for (EssenceType type : EssenceType.values()) {
            if (profile.get(type) > 0) essenceCount++;
        }
        return PROCESSING_TIME_BASE + (essenceCount - 1) * 20; // +1 second per extra essence
    }

    private void processInput() {
        ItemStack input = getInputStack();
        if (input.isEmpty()) return;

        EssenceProfile profile = EssenceProfiles.resolveBlockFromItem(input);
        if (profile == null) return;

        // Find dominant essences (top 3)
        List<EssenceValue> ranked = new ArrayList<>();
        for (EssenceType type : EssenceType.values()) {
            float value = profile.get(type);
            if (value > 0) {
                ranked.add(new EssenceValue(type, value));
            }
        }
        ranked.sort(Comparator.comparing(EssenceValue::value).reversed());

        // Extract top 1-3 essences into output slots
        int outputIndex = SLOT_OUTPUT_1;
        for (int i = 0; i < Math.min(3, ranked.size()); i++) {
            EssenceValue entry = ranked.get(i);
            Item solutionItem = getSolutionItem(entry.type);
            if (solutionItem != null) {
                setOutputStack(outputIndex, new ItemStack(solutionItem, 1));
                outputIndex++;
            }
        }

        // Consume input
        getInputStack().shrink(1);
    }

    private static Item getSolutionItem(EssenceType type) {
        return switch (type) {
            case SOLIDITY -> EItems.SOLIDITY_SOLUTION.asItem();
            case LIFE -> EItems.LIFE_SOLUTION.asItem();
            case DECAY -> EItems.DECAY_SOLUTION.asItem();
            case LIGHT -> EItems.LIGHT_SOLUTION.asItem();
            case SHADOW -> EItems.SHADOW_SOLUTION.asItem();
            case MOTION -> EItems.MOTION_SOLUTION.asItem();
            case MIND -> EItems.MIND_SOLUTION.asItem();
            case SPACETIME -> EItems.SPACETIME_SOLUTION.asItem();
            case RESONANCE -> EItems.RESONANCE_SOLUTION.asItem();
        };
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SoulCentrifugeBlockEntity be) {
        if (be.hasInput() && be.hasOutputSpace()) {
            int required = be.computeProcessingTime();
            be.processingTotal = required;
            be.processingProgress++;

            if (be.processingProgress >= required) {
                be.processInput();
                be.processingProgress = 0;
            }
            be.setChanged();
            be.syncToClient();
        } else if (!be.hasInput() && be.processingProgress > 0) {
            be.processingProgress = 0;
            be.setChanged();
            be.syncToClient();
        }
    }

    public void onInputChanged() {
        this.processingProgress = 0;
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.essentialism.soul_centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SoulCentrifugeMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ProcessingProgress", processingProgress);
        output.putInt("ProcessingTotal", processingTotal);
        output.putInt("ExpCost", expCost);
        tag.merge(output.buildResult());
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ProcessingProgress", processingProgress);
        output.putInt("ProcessingTotal", processingTotal);
        output.putInt("ExpCost", expCost);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        processingProgress = input.getIntOr("ProcessingProgress", 0);
        processingTotal = input.getIntOr("ProcessingTotal", PROCESSING_TIME_BASE);
        expCost = input.getIntOr("ExpCost", 5);
    }

    private record EssenceValue(EssenceType type, float value) {}
}
