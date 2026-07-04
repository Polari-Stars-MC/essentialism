package com.essentialism.client.screen;

import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.menu.AdvancedSpectrometerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedSpectrometerScreen extends AbstractContainerScreen<AdvancedSpectrometerMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("essentialism",
            "textures/gui/container/advanced_spectrometer.png");

    private static final int CHART_X = 100;
    private static final int CHART_Y = 17;
    private static final int BAR_WIDTH = 10;
    private static final int BAR_MAX_HEIGHT = 60;

    public AdvancedSpectrometerScreen(AdvancedSpectrometerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();
        extractor.blit(TEXTURE, x, y, 0, 0, this.getImageWidth(), this.getImageHeight(),
                this.getImageWidth(), this.getImageHeight());
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();

        if (!this.menu.isAnalysisDone()) {
            extractor.centeredText(this.getFont(),
                    Component.translatable("container.essentialism.advanced_spectrometer.scanning"),
                    x + 80, y + 40, 0xFF888888);
            return;
        }

        // Draw essence bars for slot 0 (first slot) as overview
        EssenceType[] types = EssenceType.values();
        for (int i = 0; i < types.length; i++) {
            int value = this.menu.getEssenceValue(0, i);
            int barHeight = value > 0
                    ? Math.max(1, (int) (BAR_MAX_HEIGHT * Math.min(value, 100) / 100.0F))
                    : 0;

            int barX = x + CHART_X + i * (BAR_WIDTH + 2);
            int barY = y + CHART_Y + BAR_MAX_HEIGHT - barHeight;

            extractor.fill(barX, y + CHART_Y, barX + BAR_WIDTH, y + CHART_Y + BAR_MAX_HEIGHT, 0xFF222222);
            if (barHeight > 0) {
                extractor.fill(barX, barY, barX + BAR_WIDTH, barY + barHeight, getColorForEssence(types[i]));
            }

            Component label = Component.translatable(types[i].translationKey());
            extractor.centeredText(this.getFont(), label,
                    barX + BAR_WIDTH / 2, y + CHART_Y + BAR_MAX_HEIGHT + 2, 0xFFFFFFFF);
        }
    }

    private static int getColorForEssence(EssenceType type) {
        return switch (type) {
            case SOLIDITY -> 0xFFAAAAAA;
            case LIFE -> 0xFF55FF55;
            case DECAY -> 0xFFAA0000;
            case LIGHT -> 0xFFFFAA00;
            case SHADOW -> 0xFFAA00AA;
            case MOTION -> 0xFF55FFFF;
            case MIND -> 0xFFFF55FF;
            case SPACETIME -> 0xFF5555FF;
            case RESONANCE -> 0xFFFFFF55;
        };
    }
}
