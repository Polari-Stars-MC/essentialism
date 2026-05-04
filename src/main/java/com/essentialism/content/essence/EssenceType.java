package com.essentialism.content.essence;

import net.minecraft.ChatFormatting;

public enum EssenceType {
    SOLIDITY("solidity", ChatFormatting.GRAY),
    LIFE("life", ChatFormatting.GREEN),
    DECAY("decay", ChatFormatting.DARK_RED),
    LIGHT("light", ChatFormatting.GOLD),
    SHADOW("shadow", ChatFormatting.DARK_PURPLE),
    MOTION("motion", ChatFormatting.AQUA),
    MIND("mind", ChatFormatting.LIGHT_PURPLE),
    SPACETIME("spacetime", ChatFormatting.BLUE),
    RESONANCE("resonance", ChatFormatting.YELLOW);

    private final String id;
    private final ChatFormatting color;

    EssenceType(String id, ChatFormatting color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return this.id;
    }

    public ChatFormatting color() {
        return this.color;
    }

    public String translationKey() {
        return "essence." + this.id;
    }
}
