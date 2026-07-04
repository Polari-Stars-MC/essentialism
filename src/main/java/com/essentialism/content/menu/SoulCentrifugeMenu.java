package com.essentialism.content.menu;

import com.essentialism.content.block.entity.SoulCentrifugeBlockEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Menu for the Soul Centrifuge.
 * <p>
 * 1 input slot + 3 output slots + player inventory.
 * Uses ContainerData for progress sync.
 */
public class SoulCentrifugeMenu extends AbstractContainerMenu {

    private static final int SLOT_INPUT_X = 44;
    private static final int SLOT_INPUT_Y = 20;
    private static final int SLOT_OUTPUT_1_X = 98;
    private static final int SLOT_OUTPUT_Y = 20;
    private static final int SLOT_OUTPUT_2_X = 116;
    private static final int SLOT_OUTPUT_3_X = 134;
    private static final int INV_X = 8;
    private static final int INV_Y = 84;
    private static final int HOTBAR_X = 8;
    private static final int HOTBAR_Y = 142;

    private final SoulCentrifugeBlockEntity blockEntity;
    private final ContainerData data;
    private final SimpleContainer inputContainer;
    private final SimpleContainer outputContainer;

    public SoulCentrifugeMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null, new SimpleContainerData(SoulCentrifugeBlockEntity.DATA_COUNT));
    }

    public SoulCentrifugeMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory);
    }

    public SoulCentrifugeMenu(
            int containerId,
            Inventory playerInventory,
            @Nullable SoulCentrifugeBlockEntity blockEntity,
            ContainerData data
    ) {
        super(EMenus.SOUL_CENTRIFUGE.get(), containerId);
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
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        // Output slots (take-only) — use SimpleContainer wrappers
        this.outputContainer = new SimpleContainer(3);
        if (blockEntity != null) {
            this.outputContainer.setItem(0, blockEntity.getOutputStack(SoulCentrifugeBlockEntity.SLOT_OUTPUT_1));
            this.outputContainer.setItem(1, blockEntity.getOutputStack(SoulCentrifugeBlockEntity.SLOT_OUTPUT_2));
            this.outputContainer.setItem(2, blockEntity.getOutputStack(SoulCentrifugeBlockEntity.SLOT_OUTPUT_3));
        }
        this.addSlot(new Slot(this.outputContainer, 0, SLOT_OUTPUT_1_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new Slot(this.outputContainer, 1, SLOT_OUTPUT_2_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        this.addSlot(new Slot(this.outputContainer, 2, SLOT_OUTPUT_3_X, SLOT_OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
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
            if (!this.moveItemStackTo(sourceStack, 4, 40, true)) {
                return ItemStack.EMPTY;
            }
        } else if (index >= 1 && index <= 3) {
            // From output slots to inventory
            if (!this.moveItemStackTo(sourceStack, 4, 40, true)) {
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

    public int getProcessingProgress() {
        return this.data.get(0);
    }

    public int getProcessingTotal() {
        return this.data.get(1);
    }

    public int getExpCost() {
        return this.data.get(2);
    }

    @Nullable
    public SoulCentrifugeBlockEntity getBlockEntity() {
        return this.blockEntity;
    }
}
