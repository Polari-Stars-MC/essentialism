package com.essentialism.content.menu;

import com.essentialism.content.block.entity.AdvancedSpectrometerBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the Advanced Spectrometer — 3×3 grid + player inventory.
 */
public class AdvancedSpectrometerMenu extends AbstractContainerMenu {

    private static final int GRID_START_X = 30;
    private static final int GRID_START_Y = 17;
    private static final int INV_X = 8;
    private static final int INV_Y = 84;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 142;

    private final AdvancedSpectrometerBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer gridContainer;

    public AdvancedSpectrometerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainerData(AdvancedSpectrometerBlockEntity.DATA_COUNT));
    }

    public AdvancedSpectrometerMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    public AdvancedSpectrometerMenu(
            int containerId,
            Inventory playerInventory,
            @Nullable AdvancedSpectrometerBlockEntity blockEntity,
            ContainerData data
    ) {
        super(EMenus.ADVANCED_SPECTROMETER.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // 3×3 grid
        this.gridContainer = new SimpleContainer(9);
        if (blockEntity != null) {
            for (int i = 0; i < 9; i++) {
                this.gridContainer.setItem(i, blockEntity.getItem(i));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIndex = row * 3 + col;
                this.addSlot(new Slot(this.gridContainer, slotIndex,
                        GRID_START_X + col * 18, GRID_START_Y + row * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        if (blockEntity != null) {
                            blockEntity.setItem(slotIndex, this.getItem());
                            blockEntity.onSlotsChanged();
                        }
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return true;
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, INV_X + col * 18, INV_Y + row * 18));
            }
        }
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

        if (index < 9) {
            if (!this.moveItemStackTo(sourceStack, 9, 45, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.moveItemStackTo(sourceStack, 0, 9, false)) {
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

    public int getEssenceValue(int slot, int essenceIndex) {
        if (slot < 0 || slot >= 9 || essenceIndex < 0 || essenceIndex >= 9) return 0;
        return this.data.get(slot * 9 + essenceIndex);
    }

    public boolean isAnalysisDone() {
        return blockEntity != null && blockEntity.isAnalysisDone();
    }

    @Nullable
    public AdvancedSpectrometerBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
