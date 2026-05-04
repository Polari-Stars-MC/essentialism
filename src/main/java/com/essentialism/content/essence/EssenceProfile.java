package com.essentialism.content.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
        List<EssenceValue> ranked = new ArrayList<>();
        for (EssenceType type : EssenceType.values()) {
            float value = this.get(type);
            if (value > 0.0F) {
                ranked.add(new EssenceValue(type, value));
            }
        }
        ranked.sort(Comparator.comparing(EssenceValue::value).reversed());

        List<Component> lines = new ArrayList<>();
        int limit = Math.min(3, ranked.size());
        for (int i = 0; i < limit; i++) {
            EssenceValue entry = ranked.get(i);
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

    private record EssenceValue(EssenceType type, float value) {}
}
