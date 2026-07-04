package com.essentialism.client.screen;

import com.essentialism.content.block.entity.RuneMatrixBlockEntity;
import com.essentialism.content.menu.RuneMatrixMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class RuneMatrixScreen extends AbstractContainerScreen<RuneMatrixMenu> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("essentialism",
            "textures/gui/container/rune_matrix.png");

    public RuneMatrixScreen(RuneMatrixMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 189);
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

        // Exp cost label
        int expCost = this.menu.getExpCost();
        Component expLabel = Component.translatable("container.essentialism.rune_matrix.exp_cost", expCost);
        extractor.centeredText(this.getFont(), expLabel,
                x + 88, y + 90, 0xFFAA00FF);

        // Recipe memory hint
        RuneMatrixBlockEntity be = this.menu.getBlockEntity();
        if (be != null && !be.getRecipeMemory().isEmpty()) {
            var memory = be.getRecipeMemory();
            Component lastCraft = Component.translatable("container.essentialism.rune_matrix.last_craft",
                    memory.get(memory.size() - 1));
            extractor.centeredText(this.getFont(), lastCraft,
                    x + 88, y + 6, 0xFF888888);
        }
    }
}
