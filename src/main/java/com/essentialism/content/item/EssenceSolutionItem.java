package com.essentialism.content.item;

import com.essentialism.content.essence.EssenceLevel;
import com.essentialism.content.essence.EssenceType;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A bottled concentration of a single {@link EssenceType}.
 * <p>
 * Each essence solution holds a fixed concentration value and serves as the
 * standardized medium for essence storage, transport, and recipe crafting.
 * Solutions are the bridge between "perceiving" essence and "using" it.
 */
public class EssenceSolutionItem extends Item {

    /** Default concentration for a standard solution bottle. */
    public static final float DEFAULT_CONCENTRATION = 50.0F;

    private final EssenceType essenceType;
    private final float concentration;

    public EssenceSolutionItem(Properties properties, EssenceType essenceType) {
        this(properties, essenceType, DEFAULT_CONCENTRATION);
    }

    public EssenceSolutionItem(Properties properties, EssenceType essenceType, float concentration) {
        super(properties);
        this.essenceType = essenceType;
        this.concentration = concentration;
    }

    public EssenceType getEssenceType() {
        return this.essenceType;
    }

    /**
     * Returns the concentration value of this solution.
     * Used in recipe matching and essence display.
     */
    public float getConcentration() {
        return this.concentration;
    }

    /**
     * Returns the concentration tier for display purposes.
     */
    public EssenceLevel getConcentrationLevel() {
        return EssenceLevel.of(this.concentration);
    }

    @Override
    public Component getName(ItemStack stack) {
        EssenceLevel tier = getConcentrationLevel();
        return Component.translatable(this.getDescriptionId())
                .append(" ")
                .append(Component.translatable("essence_level." + tier.name().toLowerCase()))
                .withStyle(this.essenceType.color());
    }

    // ─── Right-click behavior (recipe system entry point placeholder) ──

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(Component.translatable(
                        "message.essentialism.solution.info",
                        Component.translatable(this.essenceType.translationKey()).withStyle(this.essenceType.color()),
                        Component.literal(String.valueOf(Math.round(this.concentration)))
                ));
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(level, player, hand);
    }
}
