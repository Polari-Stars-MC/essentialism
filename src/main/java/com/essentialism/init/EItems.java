package com.essentialism.init;

import com.essentialism.Essentialism;
import com.essentialism.advancement.AnalyzerLensScanSizeTrigger;
import com.essentialism.content.block.AdvancedSpectrometerBlock;
import com.essentialism.content.block.EssenceResonanceMatrixBlock;
import com.essentialism.content.block.RuneMatrixBlock;
import com.essentialism.content.block.SimpleAnalyzerBlock;
import com.essentialism.content.block.SoulCentrifugeBlock;
import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.item.AnalyzerLensItem;
import com.essentialism.content.item.EssenceSolutionItem;
import com.essentialism.content.item.EssentialismGuideBookItem;
import com.essentialism.content.item.ParadoxItem;
import com.essentialism.content.item.ReconstructedItem;
import dev.anvilcraft.lib.v2.registrum.providers.GeneratorType;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import com.essentialism.content.block.entity.AdvancedSpectrometerBlockEntity;
import com.essentialism.content.block.entity.EssenceResonanceMatrixBlockEntity;
import com.essentialism.content.block.entity.RuneMatrixBlockEntity;
import com.essentialism.content.block.entity.SimpleAnalyzerBlockEntity;
import com.essentialism.content.block.entity.SoulCentrifugeBlockEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;
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

    public static final ItemEntry<EssenceSolutionItem> SOLIDITY_SOLUTION = Essentialism.ESSENER
            .item("solidity_solution", p -> new EssenceSolutionItem(p, EssenceType.SOLIDITY))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Solidity Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> LIFE_SOLUTION = Essentialism.ESSENER
            .item("life_solution", p -> new EssenceSolutionItem(p, EssenceType.LIFE))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Life Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> DECAY_SOLUTION = Essentialism.ESSENER
            .item("decay_solution", p -> new EssenceSolutionItem(p, EssenceType.DECAY))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Decay Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> LIGHT_SOLUTION = Essentialism.ESSENER
            .item("light_solution", p -> new EssenceSolutionItem(p, EssenceType.LIGHT))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Light Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> SHADOW_SOLUTION = Essentialism.ESSENER
            .item("shadow_solution", p -> new EssenceSolutionItem(p, EssenceType.SHADOW))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Shadow Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> MOTION_SOLUTION = Essentialism.ESSENER
            .item("motion_solution", p -> new EssenceSolutionItem(p, EssenceType.MOTION))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Motion Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> MIND_SOLUTION = Essentialism.ESSENER
            .item("mind_solution", p -> new EssenceSolutionItem(p, EssenceType.MIND))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Mind Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> SPACETIME_SOLUTION = Essentialism.ESSENER
            .item("spacetime_solution", p -> new EssenceSolutionItem(p, EssenceType.SPACETIME))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Spacetime Solution")
            .register();

    public static final ItemEntry<EssenceSolutionItem> RESONANCE_SOLUTION = Essentialism.ESSENER
            .item("resonance_solution", p -> new EssenceSolutionItem(p, EssenceType.RESONANCE))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Resonance Solution")
            .register();

    public static final BlockEntry<SoulCentrifugeBlock> SOUL_CENTRIFUGE = Essentialism.ESSENER
            .block("soul_centrifuge", SoulCentrifugeBlock::new)
            .properties(p -> p.strength(4.0F).requiresCorrectToolForDrops())
            .defaultBlockstate()
            .defaultLoot()
            .simpleItem()
            .simpleBlockEntity((type, pos, state) -> new SoulCentrifugeBlockEntity(type, pos, state))
            .lang("Soul Centrifuge")
            .recipe((ctx, prov) -> {
                prov.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("NCN")
                        .pattern("SAS")
                        .pattern("NCN")
                        .define('N', Items.NETHERITE_INGOT)
                        .define('C', Items.SCULK_CATALYST)
                        .define('S', Items.SOUL_SAND)
                        .define('A', Items.AMETHYST_BLOCK)
                        .unlockedBy("has_analyzers_lens", prov.has(EItems.ANALYZERS_LENS.get()))
                        .save(prov.getOutput());
            })
            .register();

    public static final BlockEntry<AdvancedSpectrometerBlock> ADVANCED_SPECTROMETER = Essentialism.ESSENER
            .block("advanced_spectrometer", AdvancedSpectrometerBlock::new)
            .properties(p -> p.strength(3.5F).requiresCorrectToolForDrops())
            .defaultBlockstate()
            .defaultLoot()
            .simpleItem()
            .simpleBlockEntity((type, pos, state) -> new AdvancedSpectrometerBlockEntity(type, pos, state))
            .lang("Advanced Spectrometer")
            .recipe((ctx, prov) -> {
                prov.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("QPQ")
                        .pattern("ESE")
                        .pattern("QPQ")
                        .define('Q', Items.QUARTZ_BLOCK)
                        .define('P', Items.ENDER_PEARL)
                        .define('E', Items.ENDER_EYE)
                        .define('S', EItems.SIMPLE_ANALYZER.get())
                        .unlockedBy("has_simple_analyzer", prov.has(EItems.SIMPLE_ANALYZER.get()))
                        .save(prov.getOutput());
            })
            .register();

    public static final BlockEntry<RuneMatrixBlock> RUNE_MATRIX = Essentialism.ESSENER
            .block("rune_matrix", RuneMatrixBlock::new)
            .properties(p -> p.strength(5.0F).requiresCorrectToolForDrops())
            .defaultBlockstate()
            .defaultLoot()
            .simpleItem()
            .simpleBlockEntity((type, pos, state) -> new RuneMatrixBlockEntity(type, pos, state))
            .lang("Rune Matrix Compilation Table")
            .recipe((ctx, prov) -> {
                prov.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("NAN")
                        .pattern("AEA")
                        .pattern("NAN")
                        .define('N', Items.NETHERITE_BLOCK)
                        .define('A', Items.AMETHYST_BLOCK)
                        .define('E', Items.ENCHANTING_TABLE)
                        .unlockedBy("has_analyzers_lens", prov.has(EItems.ANALYZERS_LENS.get()))
                        .save(prov.getOutput());
            })
            .register();

    // ─── Guide Book ────────────────────────────────────────────────────

    public static final ItemEntry<EssentialismGuideBookItem> GUIDE_BOOK = Essentialism.ESSENER
            .item("guide_book", EssentialismGuideBookItem::new)
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Essentialism Guide")
            .recipe((ctx, prov) -> {
                prov.shapeless(RecipeCategory.MISC, ctx.get())
                        .unlockedBy("has_analyzers_lens", prov.has(EItems.ANALYZERS_LENS.get()))
                        .requires(Items.BOOK)
                        .requires(EItems.ANALYZERS_LENS.get())
                        .save(prov.getOutput());
            })
            .register();

    // ─── Reconstructed Items ───────────────────────────────────────────

    public static final ItemEntry<ReconstructedItem> VOID_BLADE = Essentialism.ESSENER
            .item("void_blade", p -> new ReconstructedItem(p, ReconstructedItem.ReconstructedType.VOID_BLADE))
            .properties(p -> p.stacksTo(1).fireResistant())
            .defaultModel()
            .lang("Void Blade")
            .register();

    public static final ItemEntry<ReconstructedItem> TIME_BUCKET = Essentialism.ESSENER
            .item("time_bucket", p -> new ReconstructedItem(p, ReconstructedItem.ReconstructedType.TIME_BUCKET))
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Time Bucket")
            .register();

    public static final ItemEntry<ReconstructedItem> MEMORY_CRYSTAL = Essentialism.ESSENER
            .item("memory_crystal", p -> new ReconstructedItem(p, ReconstructedItem.ReconstructedType.MEMORY_CRYSTAL))
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Memory Crystal")
            .register();

    public static final ItemEntry<ReconstructedItem> MOURNING_GRASS = Essentialism.ESSENER
            .item("mourning_grass", p -> new ReconstructedItem(p, ReconstructedItem.ReconstructedType.MOURNING_GRASS))
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .lang("Mourning Grass")
            .register();

    // ─── Paradox Items ─────────────────────────────────────────────────

    public static final ItemEntry<ParadoxItem> ETERNAL_EMBER = Essentialism.ESSENER
            .item("eternal_ember", p -> new ParadoxItem(p, ParadoxItem.ParadoxType.ETERNAL_EMBER))
            .properties(p -> p.stacksTo(1).fireResistant())
            .defaultModel()
            .lang("Eternal Ember")
            .register();

    public static final ItemEntry<ParadoxItem> LIGHT_CLOAK = Essentialism.ESSENER
            .item("light_cloak", p -> new ParadoxItem(p, ParadoxItem.ParadoxType.LIGHT_CLOAK))
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Light Cloak")
            .register();

    public static final ItemEntry<ParadoxItem> STATIC_ENGINE = Essentialism.ESSENER
            .item("static_engine", p -> new ParadoxItem(p, ParadoxItem.ParadoxType.STATIC_ENGINE))
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .lang("Static Engine")
            .register();

    // ─── Essence Resonance Matrix ──────────────────────────────────────

    public static final BlockEntry<EssenceResonanceMatrixBlock> ESSENCE_RESONANCE_MATRIX = Essentialism.ESSENER
            .block("essence_resonance_matrix", EssenceResonanceMatrixBlock::new)
            .properties(p -> p.strength(5.0F).requiresCorrectToolForDrops())
            .defaultBlockstate()
            .defaultLoot()
            .simpleItem()
            .simpleBlockEntity((type, pos, state) -> new EssenceResonanceMatrixBlockEntity(type, pos, state))
            .lang("Essence Resonance Matrix")
            .recipe((ctx, prov) -> {
                prov.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("AAA")
                        .pattern("ACA")
                        .pattern("AAA")
                        .define('A', Items.AMETHYST_BLOCK)
                        .define('C', Items.SCULK_CATALYST)
                        .unlockedBy("has_amethyst_block", prov.has(Items.AMETHYST_BLOCK))
                        .save(prov.getOutput());
            })
            .register();

    public static void register() {}

    public static final BlockEntry<SimpleAnalyzerBlock> SIMPLE_ANALYZER = Essentialism.ESSENER
            .block("simple_analyzer", SimpleAnalyzerBlock::new)
            .properties(p -> p.strength(3.0F).requiresCorrectToolForDrops())
            .defaultBlockstate()
            .defaultLoot()
            .simpleItem()
            .simpleBlockEntity((type, pos, state) -> new SimpleAnalyzerBlockEntity(type, pos, state))
            .lang("Simple Analyzer")
            .recipe((ctx, prov) -> {
                prov.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("IRI")
                        .pattern("RAR")
                        .pattern("IRI")
                        .define('I', net.minecraft.world.item.Items.IRON_INGOT)
                        .define('R', Items.REDSTONE)
                        .define('A', EItems.ANALYZERS_LENS.get())
                        .unlockedBy("has_analyzers_lens", prov.has(EItems.ANALYZERS_LENS.get()))
                        .save(prov.getOutput());
            })
            .register();
}
