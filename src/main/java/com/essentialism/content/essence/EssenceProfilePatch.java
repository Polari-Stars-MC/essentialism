package com.essentialism.content.essence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record EssenceProfilePatch(
        float solidity,
        boolean hasSolidity,
        float life,
        boolean hasLife,
        float decay,
        boolean hasDecay,
        float light,
        boolean hasLight,
        float shadow,
        boolean hasShadow,
        float motion,
        boolean hasMotion,
        float mind,
        boolean hasMind,
        float spacetime,
        boolean hasSpacetime,
        float resonance,
        boolean hasResonance
) {
    public static final Codec<EssenceProfilePatch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("solidity").forGetter(patch -> patch.hasSolidity ? java.util.Optional.of(patch.solidity) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("life").forGetter(patch -> patch.hasLife ? java.util.Optional.of(patch.life) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("decay").forGetter(patch -> patch.hasDecay ? java.util.Optional.of(patch.decay) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("light").forGetter(patch -> patch.hasLight ? java.util.Optional.of(patch.light) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("shadow").forGetter(patch -> patch.hasShadow ? java.util.Optional.of(patch.shadow) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("motion").forGetter(patch -> patch.hasMotion ? java.util.Optional.of(patch.motion) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("mind").forGetter(patch -> patch.hasMind ? java.util.Optional.of(patch.mind) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("spacetime").forGetter(patch -> patch.hasSpacetime ? java.util.Optional.of(patch.spacetime) : java.util.Optional.empty()),
            Codec.FLOAT.optionalFieldOf("resonance").forGetter(patch -> patch.hasResonance ? java.util.Optional.of(patch.resonance) : java.util.Optional.empty())
    ).apply(instance, (solidity, life, decay, light, shadow, motion, mind, spacetime, resonance) -> new EssenceProfilePatch(
            solidity.orElse(0.0F), solidity.isPresent(),
            life.orElse(0.0F), life.isPresent(),
            decay.orElse(0.0F), decay.isPresent(),
            light.orElse(0.0F), light.isPresent(),
            shadow.orElse(0.0F), shadow.isPresent(),
            motion.orElse(0.0F), motion.isPresent(),
            mind.orElse(0.0F), mind.isPresent(),
            spacetime.orElse(0.0F), spacetime.isPresent(),
            resonance.orElse(0.0F), resonance.isPresent()
    )));

    public static EssenceProfilePatch of(EssenceProfile profile) {
        return new EssenceProfilePatch(
                profile.solidity(), true,
                profile.life(), true,
                profile.decay(), true,
                profile.light(), true,
                profile.shadow(), true,
                profile.motion(), true,
                profile.mind(), true,
                profile.spacetime(), true,
                profile.resonance(), true
        );
    }

    public EssenceProfile apply(EssenceProfile base) {
        return new EssenceProfile(
                this.hasSolidity ? this.solidity : base.solidity(),
                this.hasLife ? this.life : base.life(),
                this.hasDecay ? this.decay : base.decay(),
                this.hasLight ? this.light : base.light(),
                this.hasShadow ? this.shadow : base.shadow(),
                this.hasMotion ? this.motion : base.motion(),
                this.hasMind ? this.mind : base.mind(),
                this.hasSpacetime ? this.spacetime : base.spacetime(),
                this.hasResonance ? this.resonance : base.resonance()
        );
    }
}
