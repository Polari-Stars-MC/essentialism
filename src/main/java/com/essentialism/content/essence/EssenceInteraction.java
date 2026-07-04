package com.essentialism.content.essence;

/**
 * Defines the interaction relationships between pairs of {@link EssenceType}s.
 * <p>
 * Based on the design document's "Interactions Between Essences" table.
 * Each interaction type carries a gameplay implication — it should affect
 * extraction yield, recipe efficiency, resonance behavior, or particle feedback.
 *
 * @see EssenceType#interactionWith(EssenceType)
 */
public enum EssenceInteraction {
    /**
     * A weakens B; B weakens A. Mutual suppression.
     * Example: Solidity ←→ Decay (reinforced items slow decay; decay weakens solidity)
     */
    COUNTER("相克", "互相削弱"),

    /**
     * A and B both depend on and oppose each other simultaneously.
     * Example: Life ←→ Decay (life exists at decay's cost; death makes way for new life)
     */
    PARADOXICAL("相生相克", "共生互斥"),

    /**
     * A and B define each other — one's strength clarifies the other's boundary.
     * Example: Light ←→ Shadow (the stronger one, the clearer the other's edge)
     */
    MUTUAL_DEFINITION("互相定义", "对立定义"),

    /**
     * A and B compete for dominance. Extreme A tears B; folded B cancels A.
     * Example: Motion ←→ Spacetime
     */
    COMPETITION("竞争", "此消彼长"),

    /**
     * A amplifies B through connection; B needs A as anchor.
     * Example: Mind ←→ Resonance
     */
    AMPLIFICATION("放大", "共鸣放大"),

    /**
     * A resists B fundamentally; B cannot overcome A's nature.
     * Example: Solidity ←→ Spacetime (bedrock's infinite solidity comes from rejecting spacetime)
     */
    RESISTANCE("对抗", "本质排斥"),

    /**
     * A and B exist on different planes — one can have A without B, and vice versa.
     * Example: Life ←→ Mind (grass has life without mind; endermen have mind but barely life)
     */
    INCOMMENSURABLE("不可通约", "维度不同"),

    /**
     * A and B conspire — decayed things tend to hide; hidden things decay faster.
     * Example: Decay ←→ Shadow
     */
    CONSPIRACY("同谋", "加速共谋"),

    /**
     * A and B work together — one drives, the other responds.
     * Example: Resonance ←→ Motion (redstone signal drives pistons; sound triggers mechanisms)
     */
    SYNERGY("协作", "互补协同");

    private final String chineseName;
    private final String description;

    EssenceInteraction(String chineseName, String description) {
        this.chineseName = chineseName;
        this.description = description;
    }

    public String chineseName() {
        return this.chineseName;
    }

    public String description() {
        return this.description;
    }

    /**
     * Returns the interaction between two essences, or null if no special
     * interaction is defined. Order-independent: between(a,b) == between(b,a).
     */
    public static EssenceInteraction between(EssenceType a, EssenceType b) {
        if (a == b) return null;

        // Normalize order for symmetric lookups
        EssenceType first = a.ordinal() < b.ordinal() ? a : b;
        EssenceType second = a.ordinal() < b.ordinal() ? b : a;

        return switch (first) {
            case SOLIDITY -> switch (second) {
                case DECAY -> COUNTER;
                case SPACETIME -> RESISTANCE;
                default -> null;
            };
            case LIFE -> switch (second) {
                case DECAY -> PARADOXICAL;
                case MIND -> INCOMMENSURABLE;
                default -> null;
            };
            case LIGHT -> switch (second) {
                case SHADOW -> MUTUAL_DEFINITION;
                default -> null;
            };
            case SHADOW -> switch (second) {
                case DECAY -> CONSPIRACY;
                default -> null;
            };
            case MOTION -> switch (second) {
                case SPACETIME -> COMPETITION;
                default -> null;
            };
            case MIND -> switch (second) {
                case RESONANCE -> AMPLIFICATION;
                default -> null;
            };
            case RESONANCE -> switch (second) {
                case MOTION -> SYNERGY;
                default -> null;
            };
            default -> null;
        };
    }
}
