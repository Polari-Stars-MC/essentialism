package com.essentialism;

import com.essentialism.client.render.item.AnalyzerLensClientExtension;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import static com.essentialism.Essentialism.MANAGER;

@EventBusSubscriber(modid = Essentialism.MOD_ID, value = Dist.CLIENT)
public final class EssentialismClient {
    private EssentialismClient() {}

    @SubscribeEvent
    public static void commonSetup(FMLClientSetupEvent event) {
        MANAGER.loadAllClientIntegrations();
    }

    @SubscribeEvent
    public static void renderAnalyzerLensOverlay(RenderLevelStageEvent.AfterTranslucentParticles event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        AnalyzerLensClientExtension.INSTANCE.renderWorld(event);
    }
}
