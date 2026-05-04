package com.essentialism.content.essence;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum CombinedEssenceType {
    TWILIGHT("twilight", ChatFormatting.DARK_AQUA, 60.0F,
            EssenceType.LIGHT, EssenceType.SHADOW),
    PARADOX("paradox", ChatFormatting.RED, 60.0F,
            EssenceType.LIFE, EssenceType.DECAY),
    STASIS("stasis", ChatFormatting.BLUE, 60.0F,
            EssenceType.MOTION, EssenceType.SPACETIME),
    ECHO("echo", ChatFormatting.YELLOW, 60.0F,
            EssenceType.MIND, EssenceType.RESONANCE),
    ABYSS("abyss", ChatFormatting.DARK_PURPLE, 60.0F,
            EssenceType.SHADOW, EssenceType.SPACETIME);

    private final String id;
    private final ChatFormatting color;
    private final float threshold;
    private final EssenceType[] parts;

    CombinedEssenceType(String id, ChatFormatting color, float threshold, EssenceType... parts) {
        this.id = id;
        this.color = color;
        this.threshold = threshold;
        this.parts = parts;
    }

    public String translationKey() {
        return "combined_essence." + this.id;
    }

    public boolean matches(EssenceProfile profile) {
        for (EssenceType type : this.parts) {
            if (profile.get(type) < this.threshold) {
                return false;
            }
        }
        return true;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey()).withStyle(this.color);
    }

    public static List<Component> detect(EssenceProfile profile) {
        List<Component> lines = new ArrayList<>();
        for (CombinedEssenceType type : values()) {
            if (type.matches(profile)) {
                lines.add(Component.translatable(
                        "message.essentialism.analyzers_lens.combined_entry",
                        type.displayName()
                ));
            }
        }
        return lines;
    }
}
