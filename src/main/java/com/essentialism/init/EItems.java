package com.essentialism.init;

import com.essentialism.Essentialism;
import com.essentialism.advancement.AnalyzerLensScanSizeTrigger;
import com.essentialism.content.item.AnalyzerLensItem;
import dev.anvilcraft.lib.v2.registrum.providers.GeneratorType;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;
import dev.anvilcraft.lib.v2.registrum.util.entry.RegistryEntry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;

public class EItems {
    public static final RegistryEntry<CreativeModeTab, CreativeModeTab> MAIN =
            Essentialism.ESSENER.defaultCreativeTab("main", b -> b.icon(() -> EItems.ANALYZERS_LENS.get().getDefaultInstance()))
                    .register();

    public static final ItemEntry<AnalyzerLensItem> ANALYZERS_LENS = Essentialism.ESSENER
            .item("analyzers_lens", AnalyzerLensItem::new)
            .properties(p -> p.stacksTo(1))
            .setData(ProviderType.ADVANCEMENT, (ctx, prov) -> {


            })
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
            })
            .lang("Analyzer's Lens")
            .register();

    public static void register() {}
}
