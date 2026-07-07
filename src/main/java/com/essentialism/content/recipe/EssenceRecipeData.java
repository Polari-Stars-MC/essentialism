package com.essentialism.content.recipe;

import com.essentialism.content.essence.EssenceType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Data-driven essence crafting recipe.
 * <p>
 * Supports three recipe types:
 * - SEPARATION:  Item → essence solutions (used by Soul Centrifuge)
 * - TRANSFORMATION: Item + essences → variant item (used by Rune Matrix)
 * - RECONSTRUCTION: Multiple essences + catalyst → new item (used by Rune Matrix)
 * <p>
 * Recipes are loaded from JSON data packs at:
 * data/<namespace>/essentialism/essence_recipes/
 */
public class EssenceRecipeData {

    public enum Category {
        SEPARATION, TRANSFORMATION, RECONSTRUCTION;

        public static Category byName(String name) {
            try { return valueOf(name.toUpperCase()); }
            catch (IllegalArgumentException e) { return RECONSTRUCTION; }
        }
    }

    private final Category category;
    private final Ingredient input;
    private final Map<EssenceType, Float> essences;
    private final Optional<Ingredient> catalyst;
    private final ItemStack result;
    private final float minConcentration;
    private final int expCost;

    /** Cached defensive copies for hot-path getter calls. */
    private volatile Map<EssenceType, Float> cachedEssencesCopy;
    private volatile ItemStack cachedResultCopy;

    public EssenceRecipeData(
            Category category,
            Ingredient input,
            Map<EssenceType, Float> essences,
            Optional<Ingredient> catalyst,
            ItemStack result,
            float minConcentration,
            int expCost
    ) {
        this.category = category;
        this.input = input;
        this.essences = new EnumMap<>(essences);
        this.catalyst = catalyst;
        this.result = result;
        this.minConcentration = minConcentration;
        this.expCost = expCost;
    }

    public Category category() { return category; }
    public Ingredient input() { return input; }
    public Map<EssenceType, Float> essences() {
        var cached = this.cachedEssencesCopy;
        if (cached != null) return cached;
        cached = new EnumMap<>(essences);
        this.cachedEssencesCopy = cached;
        return cached;
    }
    public Optional<Ingredient> catalyst() { return catalyst; }
    public ItemStack result() {
        var cached = this.cachedResultCopy;
        if (cached != null) return cached;
        cached = result.copy();
        this.cachedResultCopy = cached;
        return cached;
    }
    public float minConcentration() { return minConcentration; }
    public int expCost() { return expCost; }

    /**
     * Check if a set of provided essences satisfies this recipe's requirements.
     */
    public boolean matches(Map<EssenceType, Float> providedEssences, ItemStack providedCatalyst) {
        for (Map.Entry<EssenceType, Float> required : this.essences.entrySet()) {
            Float provided = providedEssences.get(required.getKey());
            if (provided == null || provided < required.getValue()) return false;
        }
        if (this.catalyst.isPresent()) {
            if (providedCatalyst.isEmpty()) return false;
            if (!this.catalyst.get().test(providedCatalyst)) return false;
        }
        return true;
    }

    // ─── Codec ─────────────────────────────────────────────────────────

    public static final Codec<EssenceRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("category").forGetter(r -> r.category.name().toLowerCase()),
            Ingredient.CODEC.fieldOf("input").forGetter(EssenceRecipeData::input),
            Codec.unboundedMap(
                    Codec.STRING.xmap(EssenceType::valueOf, EssenceType::name),
                    Codec.FLOAT
            ).optionalFieldOf("essences", Map.of()).forGetter(EssenceRecipeData::essences),
            Ingredient.CODEC.optionalFieldOf("catalyst").forGetter(EssenceRecipeData::catalyst),
            ItemStack.CODEC.fieldOf("result").forGetter(EssenceRecipeData::result),
            Codec.FLOAT.optionalFieldOf("min_concentration", 30.0F).forGetter(EssenceRecipeData::minConcentration),
            Codec.INT.optionalFieldOf("exp_cost", 5).forGetter(EssenceRecipeData::expCost)
    ).apply(instance, (cat, in, ess, catOpt, res, minC, exp) -> new EssenceRecipeData(
            Category.byName(cat), in, ess, catOpt, res, minC, exp
    )));
}
