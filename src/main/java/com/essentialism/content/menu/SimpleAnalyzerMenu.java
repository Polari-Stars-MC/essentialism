package com.essentialism.content.menu;

import com.essentialism.content.block.entity.SimpleAnalyzerBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the Simple Analyzer.
 * <p>
 * One input slot (for the item to analyze) + player inventory.
 * Uses ContainerData (11 ints: 9 essence values + progress + total time)
 * for client-server sync.
 */
public class SimpleAnalyzerMenu extends AbstractContainerMenu {

    private static final int SLOT_INPUT_X = 44;
    private static final int SLOT_INPUT_Y = 35;
    private static final int INV_X = 8;
    private static final int INV_Y = 84;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 142;

    private final SimpleAnalyzerBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer inputContainer;

    public SimpleAnalyzerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainerData(SimpleAnalyzerBlockEntity.DATA_COUNT));
    }

    /**
     * Client-side constructor from network data (ForgeMenuFactory).
     */
    public SimpleAnalyzerMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    /**
     * Server-side constructor.
     */
    public SimpleAnalyzerMenu(
            int containerId,
            Inventory playerInventory,
            @Nullable SimpleAnalyzerBlockEntity blockEntity,
            ContainerData data
    ) {
        super(EMenus.SIMPLE_ANALYZER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // Input slot
        this.inputContainer = new SimpleContainer(1);
        if (blockEntity != null) {
            this.inputContainer.setItem(0, blockEntity.getInputStack());
        }
        this.addSlot(new Slot(this.inputContainer, 0, SLOT_INPUT_X, SLOT_INPUT_Y) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (blockEntity != null) {
                    blockEntity.setInputStack(this.getItem());
                    blockEntity.onInputChanged();
                }
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // Accept all items for analysis
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
        // Player hotbar
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, HOTBAR_X + col * 18, HOTBAR_Y));
        }

        this.addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index == 0) {
            // From input slot to inventory
            if (!this.moveItemStackTo(sourceStack, 1, 37, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From inventory to input slot
            if (!this.moveItemStackTo(sourceStack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null) return true;
        return this.blockEntity.getLevel() != null
                && this.blockEntity.getLevel().getBlockEntity(this.blockEntity.getBlockPos()) == this.blockEntity
                && player.distanceToSqr(
                        this.blockEntity.getBlockPos().getX() + 0.5,
                        this.blockEntity.getBlockPos().getY() + 0.5,
                        this.blockEntity.getBlockPos().getZ() + 0.5
                ) <= 64.0;
    }

    public int getEssenceValue(int essenceIndex) {
        if (essenceIndex < 0 || essenceIndex >= 9) return 0;
        return this.data.get(essenceIndex);
    }

    public int getProcessingProgress() {
        return this.data.get(9);
    }

    public int getProcessingTotal() {
        return this.data.get(10);
    }

    @Nullable
    public SimpleAnalyzerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
