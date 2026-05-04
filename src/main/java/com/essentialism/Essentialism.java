package com.essentialism;

import com.essentialism.config.EssenceConfig;
import com.essentialism.content.essence.EssenceProfiles;
import com.essentialism.data.EssenceProfileProvider;
import com.essentialism.init.EItems;
import com.essentialism.network.EssenceConfigS2CPacket;
import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.lib.v2.integration.IntegrationManager;
import dev.anvilcraft.lib.v2.registrum.Registrum;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;
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

@Mod(Essentialism.MOD_ID)
@EventBusSubscriber(modid = Essentialism.MOD_ID)
public final class Essentialism {
    public static final String MOD_ID = "essentialism";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrum REGISTRUM = Registrum.create(MOD_ID);
    public static final IntegrationManager MANAGER = new IntegrationManager(MOD_ID);
    public static final EssenceConfig ESSENCE_CONFIG = ConfigManager.register(MOD_ID, EssenceConfig::new);

    public Essentialism(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing {}", MOD_ID);
        modEventBus.addListener(EssenceProfiles::register);
        REGISTRUM.addDataGenerator(ProviderType.GENERIC_SERVER,
                provider -> {
                    provider.add(data -> new EssenceProfileProvider(data.output()));
                });

        REGISTRUM.addRawLang("essence.solidity", "Solidity");
        REGISTRUM.addRawLang("essence.life", "Life");
        REGISTRUM.addRawLang("essence.decay", "Decay");
        REGISTRUM.addRawLang("essence.light", "Light");
        REGISTRUM.addRawLang("essence.shadow", "Shadow");
        REGISTRUM.addRawLang("essence.motion", "Motion");
        REGISTRUM.addRawLang("essence.mind", "Mind");
        REGISTRUM.addRawLang("essence.spacetime", "Spacetime");
        REGISTRUM.addRawLang("essence.resonance", "Resonance");
        REGISTRUM.addRawLang("combined_essence.twilight", "Twilight");
        REGISTRUM.addRawLang("combined_essence.paradox", "Paradox");
        REGISTRUM.addRawLang("combined_essence.stasis", "Stasis");
        REGISTRUM.addRawLang("combined_essence.echo", "Echo");
        REGISTRUM.addRawLang("combined_essence.abyss", "Abyss");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.header", "Analyzing %s");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.hint", "Sneak while using the lens to inspect the full spectrum.");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.detail", "Full spectrum");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.dominant_entry", "Dominant: %s %s");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.combined", "Combined essences");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.combined_entry", "Combined: %s");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.unknown", "%s has no readable essence signature yet.");
        REGISTRUM.addRawLang("message.essentialism.analyzers_lens.empty", "No measurable essence detected.");
        EItems.register();
        MANAGER.compileContent();
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        MANAGER.loadAllIntegrations();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, new EssenceConfigS2CPacket(ESSENCE_CONFIG.isPlayerEssence));
        }
    }
}
