package com.essentialism.client.screen;

import com.essentialism.content.menu.SoulCentrifugeMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class SoulCentrifugeScreen extends AbstractContainerScreen<SoulCentrifugeMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("essentialism",
            "textures/gui/container/soul_centrifuge.png");

    public SoulCentrifugeScreen(SoulCentrifugeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();

        extractor.blit(TEXTURE, x, y, 0, 0, this.getImageWidth(), this.getImageHeight(),
                this.getImageWidth(), this.getImageHeight());

        // Progress arrow
        int progress = this.menu.getProcessingProgress();
        int total = this.menu.getProcessingTotal();
        if (total > 0) {
            int arrowWidth = (int) (22.0F * progress / total);
            extractor.blit(TEXTURE, x + 73, y + 21,
                    176, 0, arrowWidth, 16,
                    this.getImageWidth(), this.getImageHeight());
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor extractor, int mouseX, int mouseY, float partialTick) {
        super.extractContents(extractor, mouseX, mouseY, partialTick);
        int x = this.getLeftPos();
        int y = this.getTopPos();

        // Exp cost label
        int expCost = this.menu.getExpCost();
        Component expLabel = Component.translatable("container.essentialism.soul_centrifuge.exp_cost", expCost);
        extractor.centeredText(this.getFont(), expLabel,
                x + 88, y + 60, 0xFF55FF55);
    }
}
