package com.essentialism.content.block.entity;

import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.item.EssenceSolutionItem;
import com.essentialism.content.menu.RuneMatrixMenu;
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

import java.util.*;

/**
 * Rune Matrix Compilation Table — reconstructs items from essence solutions.
 * <p>
 * 9 essence input slots (3×3) + 1 catalyst slot + 1 output slot.
 * Instant crafting after confirmation; consumes experience levels.
 * Remembers the last 10 recipes for UI display.
 */
public class RuneMatrixBlockEntity extends BaseBlockEntity implements MenuProvider {

    public static final int SLOT_COUNT = 11; // 9 essence + 1 catalyst + 1 output
    public static final int SLOT_CATALYST = 9;
    public static final int SLOT_OUTPUT = 10;
    public static final int DATA_COUNT = 4; // progress, expCost, recipeCount, selectedRecipe
    public static final int MAX_MEMORY = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    protected int processingProgress = 0;
    protected int expCost = 10;
    protected int recipeCount = 0;

    /** Recipe memory: stores the output display names of recent crafts */
    private final List<Component> recipeMemory = new ArrayList<>(MAX_MEMORY);

    public RuneMatrixBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> processingProgress;
                case 1 -> expCost;
                case 2 -> recipeCount;
                case 3 -> 0; // selectedRecipe (UI-only state)
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> processingProgress = value;
                case 1 -> expCost = value;
                case 2 -> recipeCount = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public ItemStack getEssenceStack(int slot) {
        return items.get(slot);
    }

    public void setEssenceStack(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    public ItemStack getCatalystStack() {
        return items.get(SLOT_CATALYST);
    }

    public void setCatalystStack(ItemStack stack) {
        items.set(SLOT_CATALYST, stack);
    }

    public ItemStack getOutputStack() {
        return items.get(SLOT_OUTPUT);
    }

    public void setOutputStack(ItemStack stack) {
        items.set(SLOT_OUTPUT, stack);
    }

    public List<Component> getRecipeMemory() {
        return Collections.unmodifiableList(recipeMemory);
    }

    public void addRecipeToMemory(Component result) {
        if (recipeMemory.size() >= MAX_MEMORY) {
            recipeMemory.remove(0);
        }
        recipeMemory.add(result);
        this.recipeCount = recipeMemory.size();
    }

    public boolean hasEssenceInput() {
        for (int i = 0; i < 9; i++) {
            if (!items.get(i).isEmpty()) return true;
        }
        return false;
    }

    public boolean hasOutputSpace() {
        return getOutputStack().isEmpty();
    }

    /**
     * Scans the 3×3 essence grid and computes a combined EssenceProfile.
     * Returns the merged profile, or null if no essence items are present.
     */
    @Nullable
    public Map<EssenceType, Float> computeCombinedEssence() {
        Map<EssenceType, Float> combined = new EnumMap<>(EssenceType.class);
        boolean hasAny = false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof EssenceSolutionItem solution) {
                combined.merge(solution.getEssenceType(), solution.getConcentration(), Float::sum);
                hasAny = true;
            }
        }

        return hasAny ? combined : null;
    }

    /**
     * Attempts to craft using the current essence configuration.
     * This is a data-pack-extensible entry point — matching logic
     * can be overridden by server-side recipe JSONs in the future.
     */
    public boolean tryCraft(Player player) {
        Map<EssenceType, Float> essence = computeCombinedEssence();
        if (essence == null) return false;

        // For now: if any essence exceeds 50 concentration, produce a generic result
        // In the future this will be replaced by proper recipe matching
        EssenceType dominant = null;
        float maxConcentration = 0;
        for (Map.Entry<EssenceType, Float> entry : essence.entrySet()) {
            if (entry.getValue() > maxConcentration) {
                maxConcentration = entry.getValue();
                dominant = entry.getKey();
            }
        }

        if (dominant == null || maxConcentration < 30) return false;

        // Produce a concentrated solution as output
        ItemStack result = createConcentratedSolution(dominant, maxConcentration);
        if (result.isEmpty()) return false;

        // Consume inputs
        for (int i = 0; i < 9; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                stack.shrink(1);
            }
        }

        // Place result
        setOutputStack(result);
        addRecipeToMemory(result.getHoverName());
        this.expCost = Math.max(5, (int) (maxConcentration / 10));
        return true;
    }

    private ItemStack createConcentratedSolution(EssenceType type, float concentration) {
        var itemEntry = type.solutionItem();
        return new ItemStack(itemEntry.asItem(), 1);
    }

    public void onSlotsChanged() {
        this.processingProgress = 0;
        // Clear output when input changes
        if (!getOutputStack().isEmpty() && hasEssenceInput()) {
            setOutputStack(ItemStack.EMPTY);
        }
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.essentialism.rune_matrix");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new RuneMatrixMenu(containerId, playerInventory, this, this.dataAccess);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(
                net.minecraft.util.ProblemReporter.DISCARDING, registries);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ExpCost", expCost);
        output.putInt("RecipeCount", recipeCount);
        tag.merge(output.buildResult());
        return tag;
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("ExpCost", expCost);
        output.putInt("RecipeCount", recipeCount);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
        expCost = input.getIntOr("ExpCost", 10);
        recipeCount = input.getIntOr("RecipeCount", 0);
    }
}
