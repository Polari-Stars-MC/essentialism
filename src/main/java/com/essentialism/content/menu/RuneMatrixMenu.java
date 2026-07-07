package com.essentialism.content.menu;

import com.essentialism.content.block.entity.RuneMatrixBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the Rune Matrix Compilation Table.
 * <p>
 * 3×3 essence grid + 1 catalyst slot + 1 output slot + player inventory.
 */
public class RuneMatrixMenu extends AbstractContainerMenu {

    private static final int GRID_START_X = 30;
    private static final int GRID_START_Y = 17;
    private static final int CATALYST_X = 30;
    private static final int CATALYST_Y = 75;
    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 40;
    private static final int INV_X = 8;
    private static final int INV_Y = 105;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 163;

    private final RuneMatrixBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer essenceContainer;
    private final SimpleContainer catalystContainer;
    private final SimpleContainer outputContainer;

    public RuneMatrixMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainerData(RuneMatrixBlockEntity.DATA_COUNT));
    }

    public RuneMatrixMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    public RuneMatrixMenu(
            int containerId,
            Inventory playerInventory,
            @Nullable RuneMatrixBlockEntity blockEntity,
            ContainerData data
    ) {
        super(EMenus.RUNE_MATRIX.get(), containerId);
        this.blockEntity = blockEntity;
        this.data = data;

        // 3×3 essence grid
        this.essenceContainer = new SimpleContainer(9);
        if (blockEntity != null) {
            for (int i = 0; i < 9; i++) {
                this.essenceContainer.setItem(i, blockEntity.getEssenceStack(i));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                final int slotIndex = row * 3 + col;
                this.addSlot(new Slot(this.essenceContainer, slotIndex,
                        GRID_START_X + col * 18, GRID_START_Y + row * 18) {
                    @Override
                    public void setChanged() {
                        super.setChanged();
                        if (blockEntity != null) {
                            blockEntity.setEssenceStack(slotIndex, this.getItem());
                            blockEntity.onSlotsChanged();
                        }
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return stack.getItem() instanceof com.essentialism.content.item.EssenceSolutionItem;
                    }

                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                });
            }
        }

        // Catalyst slot
        this.catalystContainer = new SimpleContainer(1);
        if (blockEntity != null) {
            this.catalystContainer.setItem(0, blockEntity.getCatalystStack());
        }
        this.addSlot(new Slot(this.catalystContainer, 0, CATALYST_X, CATALYST_Y) {
            @Override
            public void setChanged() {
                super.setChanged();
                if (blockEntity != null) {
                    blockEntity.setCatalystStack(this.getItem());
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

        // Output slot (take-only)
        this.outputContainer = new SimpleContainer(1);
        if (blockEntity != null) {
            this.outputContainer.setItem(0, blockEntity.getOutputStack());
        }
        this.addSlot(new Slot(this.outputContainer, 0, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player inventory (shifted for taller GUI)
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

        int invStart = 11;
        int invEnd = 47;

        if (index < 9) {
            // From essence grid to inventory
            if (!this.moveItemStackTo(sourceStack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index == 9) {
            // From catalyst to inventory
            if (!this.moveItemStackTo(sourceStack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index == 10) {
            // From output to inventory
            if (!this.moveItemStackTo(sourceStack, invStart, invEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From inventory to essence grid or catalyst
            if (sourceStack.getItem() instanceof com.essentialism.content.item.EssenceSolutionItem) {
                if (!this.moveItemStackTo(sourceStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(sourceStack, 9, 10, false)) {
                // Try catalyst slot
                if (!this.moveItemStackTo(sourceStack, 0, 9, false)) {
                    return ItemStack.EMPTY;
                }
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
        return EMenus.stillValid(blockEntity, player);
    }

    public int getExpCost() {
        return this.data.get(1);
    }

    public int getRecipeCount() {
        return this.data.get(2);
    }

    @Nullable
    public RuneMatrixBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
