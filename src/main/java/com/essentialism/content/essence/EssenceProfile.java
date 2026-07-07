package com.essentialism.content.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public record EssenceProfile(
        float solidity,
        float life,
        float decay,
        float light,
        float shadow,
        float motion,
        float mind,
        float spacetime,
        float resonance
) {
    public static final Codec<EssenceProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("solidity", 0.0F).forGetter(EssenceProfile::solidity),
            Codec.FLOAT.optionalFieldOf("life", 0.0F).forGetter(EssenceProfile::life),
            Codec.FLOAT.optionalFieldOf("decay", 0.0F).forGetter(EssenceProfile::decay),
            Codec.FLOAT.optionalFieldOf("light", 0.0F).forGetter(EssenceProfile::light),
            Codec.FLOAT.optionalFieldOf("shadow", 0.0F).forGetter(EssenceProfile::shadow),
            Codec.FLOAT.optionalFieldOf("motion", 0.0F).forGetter(EssenceProfile::motion),
            Codec.FLOAT.optionalFieldOf("mind", 0.0F).forGetter(EssenceProfile::mind),
            Codec.FLOAT.optionalFieldOf("spacetime", 0.0F).forGetter(EssenceProfile::spacetime),
            Codec.FLOAT.optionalFieldOf("resonance", 0.0F).forGetter(EssenceProfile::resonance)
    ).apply(instance, EssenceProfile::new));

    private static final DecimalFormat VALUE_FORMAT = new DecimalFormat("0.##");

    // External cache for computed components — Java records can't have non-static fields
    private static final ConcurrentHashMap<EssenceProfile, Component> SUMMARY_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<EssenceProfile, Component> DETAILED_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<EssenceProfile, List<Component>> DOMINANT_CACHE = new ConcurrentHashMap<>();

    public float get(EssenceType type) {
        return switch (type) {
            case SOLIDITY -> this.solidity;
            case LIFE -> this.life;
            case DECAY -> this.decay;
            case LIGHT -> this.light;
            case SHADOW -> this.shadow;
            case MOTION -> this.motion;
            case MIND -> this.mind;
            case SPACETIME -> this.spacetime;
            case RESONANCE -> this.resonance;
        };
    }

    public Component summaryComponent() {
        return SUMMARY_CACHE.computeIfAbsent(this, EssenceProfile::buildSummaryComponent);
    }

    private Component buildSummaryComponent() {
        MutableComponent text = Component.empty();
        boolean first = true;
        for (EssenceType type : EssenceType.values()) {
            float value = this.get(type);
            if (value <= 0.0F) {
                continue;
            }
            if (!first) {
                text.append(Component.literal(" | "));
            }
            text.append(Component.translatable(type.translationKey()).withStyle(type.color()));
            text.append(Component.literal(" " + formatValue(value)));
            first = false;
        }
        if (first) {
            return Component.translatable("message.essentialism.analyzers_lens.empty");
        }
        return text;
    }

    public Component detailedComponent() {
        return DETAILED_CACHE.computeIfAbsent(this, EssenceProfile::buildDetailedComponent);
    }

    private Component buildDetailedComponent() {
        MutableComponent text = Component.empty();
        boolean first = true;
        for (EssenceType type : EssenceType.values()) {
            if (!first) {
                text.append(Component.literal(" | "));
            }
            text.append(Component.translatable(type.translationKey()).withStyle(type.color()));
            text.append(Component.literal(" " + formatValue(this.get(type))));
            first = false;
        }
        return text;
    }

    public List<Component> dominantComponents() {
        return DOMINANT_CACHE.computeIfAbsent(this, EssenceProfile::buildDominantComponents);
    }

    private List<Component> buildDominantComponents() {
        List<EssenceValue> ranked = topEssences(3);
        List<Component> lines = new ArrayList<>(ranked.size());
        for (EssenceValue entry : ranked) {
            lines.add(Component.translatable(
                    "message.essentialism.analyzers_lens.dominant_entry",
                    Component.translatable(entry.type.translationKey()).withStyle(entry.type.color()),
                    formatValue(entry.value)
            ));
        }
        return lines;
    }

    private static String formatValue(float value) {
        return VALUE_FORMAT.format(value);
    }

    public record EssenceValue(EssenceType type, float value) {}

    /**
     * Returns the top-N essence types by value, sorted descending.
     * Used by dominantComponents() and SoulCentrifuge processing.
     */
    public List<EssenceValue> topEssences(int limit) {
        List<EssenceValue> ranked = new ArrayList<>();
        for (EssenceType type : EssenceType.values()) {
            float value = this.get(type);
            if (value > 0.0F) {
                ranked.add(new EssenceValue(type, value));
            }
        }
        ranked.sort(Comparator.comparing(EssenceValue::value).reversed());
        return ranked.size() > limit ? ranked.subList(0, limit) : ranked;
    }
}
