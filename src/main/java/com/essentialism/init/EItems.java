package com.essentialism.init;

import com.essentialism.Essentialism;
import com.essentialism.content.item.AnalyzerLensItem;
import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class EItems {
    public static final ItemEntry<AnalyzerLensItem> ANALYZERS_LENS = Essentialism.REGISTRUM
            .item("analyzers_lens", AnalyzerLensItem::new)
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .recipe((ctx, prov) -> {
                prov.shapeless(RecipeCategory.MISC, ctx.get())
                        .unlockedBy("has_amethyst_shard", prov.has(Items.AMETHYST_SHARD))
                        .unlockedBy("has_copper_ingot", prov.has(Items.COPPER_INGOT))
                        .unlockedBy("has_glass_pane", prov.has(Items.GLASS_PANE))
                        .requires(Items.AMETHYST_SHARD)
                        .requires(Items.COPPER_INGOT)
                        .requires(Items.GLASS_PANE)
                        .save(prov.getOutput());
                        ;
            })
            .lang("Analyzer's Lens")
            .register();

    public static void register() {}
}
