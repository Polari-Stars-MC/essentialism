package com.essentialism.content.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EntityEssenceProfileDefinition(boolean replace, EssenceProfilePatch profile) {
    public static final Codec<EntityEssenceProfileDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(EntityEssenceProfileDefinition::replace),
            EssenceProfilePatch.CODEC.fieldOf("profile").forGetter(EntityEssenceProfileDefinition::profile)
    ).apply(instance, EntityEssenceProfileDefinition::new));
}
