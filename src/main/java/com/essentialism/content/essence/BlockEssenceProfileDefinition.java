package com.essentialism.content.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlockEssenceProfileDefinition(boolean replace, EssenceProfilePatch profile) {
    public static final Codec<BlockEssenceProfileDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("replace", false).forGetter(BlockEssenceProfileDefinition::replace),
            EssenceProfilePatch.CODEC.fieldOf("profile").forGetter(BlockEssenceProfileDefinition::profile)
    ).apply(instance, BlockEssenceProfileDefinition::new));
}
