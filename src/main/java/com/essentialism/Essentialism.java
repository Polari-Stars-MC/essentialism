package com.essentialism;

import com.essentialism.advancement.AnalyzerLensScanSizeTrigger;
import com.essentialism.config.EssenceConfig;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.content.item.AnalyzerLensItem;
import com.essentialism.data.EssenceProfileProvider;
import com.essentialism.init.EAttachments;
import com.essentialism.init.EItems;
import com.essentialism.init.ETriggers;
import com.essentialism.essener.Essener;
import com.essentialism.network.EssenceConfigS2CPacket;
import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.lib.v2.integration.IntegrationManager;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.util.Optional;

@Mod(Essentialism.MOD_ID)
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class Essentialism {
    public static final String MOD_ID = "essentialism";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Essener ESSENER = Essener.create(MOD_ID);
    public static final IntegrationManager MANAGER = new IntegrationManager(MOD_ID);
    public static final EssenceConfig ESSENCE_CONFIG = ConfigManager.register(MOD_ID, EssenceConfig::new);

    public Essentialism(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing {}", MOD_ID);
        modEventBus.addListener(EssenceProfiles::register);
        ESSENER.addDataGenerator(ProviderType.GENERIC_SERVER,
                provider -> {
                    provider.add(data -> new EssenceProfileProvider(data.output()));
                });

        ESSENER.addRawLang("essence.solidity", "Solidity");
        ESSENER.addRawLang("essence.life", "Life");
        ESSENER.addRawLang("essence.decay", "Decay");
        ESSENER.addRawLang("essence.light", "Light");
        ESSENER.addRawLang("essence.shadow", "Shadow");
        ESSENER.addRawLang("essence.motion", "Motion");
        ESSENER.addRawLang("essence.mind", "Mind");
        ESSENER.addRawLang("essence.spacetime", "Spacetime");
        ESSENER.addRawLang("essence.resonance", "Resonance");
        ESSENER.addRawLang("combined_essence.twilight", "Twilight");
        ESSENER.addRawLang("combined_essence.paradox", "Paradox");
        ESSENER.addRawLang("combined_essence.stasis", "Stasis");
        ESSENER.addRawLang("combined_essence.echo", "Echo");
        ESSENER.addRawLang("combined_essence.abyss", "Abyss");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.header", "Analyzing %s");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.hint", "Sneak while using the lens to inspect the full spectrum.");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.detail", "Full spectrum");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.dominant_entry", "Dominant: %s %s");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.combined", "Combined essences");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.combined_entry", "Combined: %s");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.unknown", "%s has no readable essence signature yet.");
        ESSENER.addRawLang("message.essentialism.analyzers_lens.empty", "No measurable essence detected.");
        ESSENER.addRawLang("advancement.essentialism.root.title", "Essentialism");
        ESSENER.addRawLang("advancement.essentialism.root.description", "Reveal the hidden signatures woven through the world.");
        ESSENER.addRawLang("advancement.essentialism.scan_50_blocks.title", "Field Research");
        ESSENER.addRawLang(
                "advancement.essentialism.scan_50_blocks.description",
                "Use the Analyzer's Lens to catalog 50 different blocks."
        );
        ESSENER.addDataGenerator(ProviderType.ADVANCEMENT, prov -> {
            AdvancementHolder root = Advancement.Builder.advancement()
                    .display(
                            EItems.ANALYZERS_LENS.get(),
                            Component.translatable("advancement.essentialism.root.title"),
                            Component.translatable("advancement.essentialism.root.description"),
                            Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/stone.png"),
                            AdvancementType.TASK,
                            false, false, false)
                    .addCriterion("has_analyzers_lens", InventoryChangeTrigger.TriggerInstance.hasItems(EItems.ANALYZERS_LENS.get()))
                    .save(prov, id("root"));
            Advancement.Builder.advancement()
                    .parent(root)
                    .display(
                            EItems.ANALYZERS_LENS.get(),
                            Component.translatable("advancement.essentialism.scan_50_blocks.title"),
                            Component.translatable("advancement.essentialism.scan_50_blocks.description"),
                            null,
                            AdvancementType.TASK,
                            true,
                            false,
                            false
                    )
                    .addCriterion(
                            "scan_count",
                            ETriggers.ANALYZER_LENS_SCAN_SIZE.get().createCriterion(
                                    new AnalyzerLensScanSizeTrigger.TriggerInstance(
                                            Optional.empty(),
                                            MinMaxBounds.Ints.atLeast(8)
                                    )
                            )
                    )
                    .save(prov, id("adventure/scan_50_blocks"));
        });
        ETriggers.register();
        EAttachments.register();
        EItems.register();
        MANAGER.compileContent();
    }

    private static Identifier id(String s) {
        return Identifier.fromNamespaceAndPath(MOD_ID, s);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        MANAGER.loadAllIntegrations();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            AnalyzerLensItem.syncPlayerProgress(player);
            PacketDistributor.sendToPlayer(player, new EssenceConfigS2CPacket(ESSENCE_CONFIG.isPlayerEssence));
        }
    }
}
