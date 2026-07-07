package com.essentialism.client.screen;

import com.essentialism.content.essence.EssenceType;
import com.essentialism.content.menu.SimpleAnalyzerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SimpleAnalyzerScreen extends AbstractContainerScreen<SimpleAnalyzerMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("essentialism",
            "textures/gui/container/simple_analyzer.png");

    private static final int BAR_X_START = 134;
    private static final int BAR_WIDTH = 18;
    private static final int BAR_MAX_HEIGHT = 60;
    private static final int BAR_Y_BASE = 28;

    public SimpleAnalyzerScreen(SimpleAnalyzerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 196, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();

        extractor.blit(TEXTURE, x, y, 0, 0, this.getImageWidth(), this.getImageHeight(),
                this.getImageWidth(), this.getImageHeight());

        int progress = this.menu.getProcessingProgress();
        int total = this.menu.getProcessingTotal();
        if (total > 0) {
            int arrowHeight = (int) (22.0F * progress / total);
            extractor.blit(TEXTURE, x + 79, y + 34,
                    196, 0, 22, arrowHeight,
                    this.getImageWidth(), this.getImageHeight());
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();

        EssenceType[] types = EssenceType.values();
        for (int i = 0; i < types.length; i++) {
            int value = this.menu.getEssenceValue(i);
            int barHeight = value > 0
                    ? Math.max(1, (int) (BAR_MAX_HEIGHT * Math.min(value, 100) / 100.0F))
                    : 0;

            int barX = x + BAR_X_START + i * (BAR_WIDTH + 1);
            int barY = y + BAR_Y_BASE + BAR_MAX_HEIGHT - barHeight;

            extractor.fill(barX, y + BAR_Y_BASE, barX + BAR_WIDTH, y + BAR_Y_BASE + BAR_MAX_HEIGHT, 0xFF222222);

            if (barHeight > 0) {
                int color = types[i].argbColor();
                extractor.fill(barX, barY, barX + BAR_WIDTH, barY + barHeight, color);
                extractor.outline(barX, barY, BAR_WIDTH, barHeight, 0xFF555555);
            }

            Component label = Component.translatable(types[i].translationKey());
            extractor.centeredText(this.getFont(), label,
                    barX + BAR_WIDTH / 2, y + BAR_Y_BASE + BAR_MAX_HEIGHT + 4, 0xFFFFFFFF);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(extractor, mouseX, mouseY, partialTick);

        EssenceType[] types = EssenceType.values();
        int x = this.getLeftPos();
        int y = this.getTopPos();
        for (int i = 0; i < types.length; i++) {
            int barX = x + BAR_X_START + i * (BAR_WIDTH + 1);
            int barY = y + BAR_Y_BASE;
            if (mouseX >= barX && mouseX < barX + BAR_WIDTH
                    && mouseY >= barY && mouseY < barY + BAR_MAX_HEIGHT) {
                int value = this.menu.getEssenceValue(i);
                Component tooltip = Component.translatable(types[i].translationKey())
                        .append(": " + value);
                extractor.text(this.getFont(), tooltip, mouseX, mouseY - 10, 0xFFFFFFFF);
                break;
            }
        }
    }
}
