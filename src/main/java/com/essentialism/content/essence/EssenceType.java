package com.essentialism.content.essence;

import net.minecraft.ChatFormatting;

/**
 * The nine fundamental essences that compose all things in the world.
 * <p>
 * Each essence represents a primordial aspect of existence — solidity,
 * life, decay, light, shadow, motion, mind, spacetime, and resonance.
 * Together they form the "language" through which players perceive,
 * extract, and reconstruct the world.
 * <p>
 * Design rule: interaction relationships should be few but firm.
 * Every interaction must be observable (particles, sound, UI) and
 * serve a gameplay decision (gathering, combat, building, automation).
 */
public enum EssenceType {
    SOLIDITY("solidity", ChatFormatting.GRAY,
            "我是基岩的叹息，是钻石的傲慢。",
            "坚固者的悲剧在于，世界塌陷时，他们总是最后才知道。"),

    LIFE("life", ChatFormatting.GREEN,
            "你的每一次盛开，都只是发给我的邀请函。",
            "生命是一场与熵增的共谋。活着，就是在缓慢地死去。"),

    DECAY("decay", ChatFormatting.DARK_RED,
            "你们的狂欢，终归是我的遗产。",
            "熵增从不催促。它只是等待，因为时间站在它那边。"),

    LIGHT("light", ChatFormatting.GOLD,
            "你的黑暗不过是我的幕布。",
            "光明从不照亮黑暗，它只是收回了自己的影。"),

    SHADOW("shadow", ChatFormatting.DARK_PURPLE,
            "没有你，我只是一片平等的虚无。",
            "暗影不是光的反面。光是暗影的伤口。"),

    MOTION("motion", ChatFormatting.AQUA,
            "停下？停下就是死亡！",
            "静止是运动的特例，正如死亡是生命的特例。"),

    MIND("mind", ChatFormatting.LIGHT_PURPLE,
            "没有一双眼睛，光明算什么光明？",
            "心灵是一座监狱，囚禁者既是狱卒，也是囚徒。"),

    SPACETIME("spacetime", ChatFormatting.BLUE,
            "你们争论的都是'一物'，而我管理的是'万物之间'。",
            "时空从不发言。它只是悄悄地把'这里'变成'那里'。"),

    RESONANCE("resonance", ChatFormatting.YELLOW,
            "没有我，你们只是独奏。",
            "共鸣是最安静的暴力，它让你以为'在一起'是你自己的决定。");

    private final String id;
    private final ChatFormatting color;
    private final String sigil;
    private final String philosophyNote;

    EssenceType(String id, ChatFormatting color, String sigil, String philosophyNote) {
        this.id = id;
        this.color = color;
        this.sigil = sigil;
        this.philosophyNote = philosophyNote;
    }

    public String id() {
        return this.id;
    }

    public ChatFormatting color() {
        return this.color;
    }

    /**
     * The sigil — a poetic one-liner that embodies the essence's voice.
     * Used in lore tooltips, item descriptions, and flavor text.
     */
    public String sigil() {
        return this.sigil;
    }

    /**
     * The philosophy note — a reflective observation about the essence's
     * nature. Used in guidebook entries, advancement descriptions, and
     * deep lore contexts.
     */
    public String philosophyNote() {
        return this.philosophyNote;
    }

    public String translationKey() {
        return "essence." + this.id;
    }

    /**
     * Returns the concentration tier for a given essence value.
     *
     * @param value the essence concentration
     * @return the matching {@link EssenceLevel}
     */
    public static EssenceLevel levelOf(float value) {
        return EssenceLevel.of(value);
    }

    /**
     * Returns the concentration tier for a given integer value.
     */
    public static EssenceLevel levelOf(int value) {
        return EssenceLevel.of(value);
    }

    // ─── Interaction System ───────────────────────────────────────────

    /**
     * Returns the interaction relationship between two essences.
     *
     * @param a the first essence
     * @param b the second essence
     * @return the interaction result, or null if no special interaction exists
     */
    public static EssenceInteraction getInteraction(EssenceType a, EssenceType b) {
        return EssenceInteraction.between(a, b);
    }

    /**
     * Returns the interaction between this essence and another.
     */
    public EssenceInteraction interactionWith(EssenceType other) {
        return EssenceInteraction.between(this, other);
    }

    // ─── Tool Efficiency ──────────────────────────────────────────────

    /**
     * Represents tool types for essence extraction efficiency.
     */
    public enum ToolType {
        PICKAXE("镐"),
        AXE("斧"),
        SHOVEL("锹"),
        SHEARS("剪刀"),
        HAND("手"),
        SILK_TOUCH("精准采集"),
        FORTUNE("时运");

        private final String chineseName;

        ToolType(String chineseName) {
            this.chineseName = chineseName;
        }

        public String chineseName() {
            return this.chineseName;
        }
    }

    /**
     * Represents extraction efficiency for a tool-essence pair.
     */
    public enum ToolEfficiency {
        NONE("无"),
        VERY_LOW("极低"),
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        ENHANCED("增强"),
        SPECIAL("特殊效果");

        private final String chineseName;

        ToolEfficiency(String chineseName) {
            this.chineseName = chineseName;
        }

        public String chineseName() {
            return this.chineseName;
        }
    }

    /**
     * Returns the extraction efficiency of a given tool for this essence.
     * <p>
     * Based on the design document's "Collection Efficiency Table".
     */
    public ToolEfficiency toolEfficiency(ToolType tool) {
        return switch (this) {
            case SOLIDITY -> switch (tool) {
                case PICKAXE -> ToolEfficiency.HIGH;
                case AXE -> ToolEfficiency.MEDIUM;
                case SHOVEL -> ToolEfficiency.LOW;
                case SHEARS -> ToolEfficiency.VERY_LOW;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 保持完整性
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加坚固量
            };
            case LIFE -> switch (tool) {
                case PICKAXE -> ToolEfficiency.NONE;
                case AXE -> ToolEfficiency.HIGH;
                case SHOVEL -> ToolEfficiency.MEDIUM;
                case SHEARS -> ToolEfficiency.HIGH;
                case HAND -> ToolEfficiency.LOW;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 冻结生命值
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加生命量
            };
            case DECAY -> switch (tool) {
                case PICKAXE -> ToolEfficiency.MEDIUM;
                case AXE -> ToolEfficiency.LOW;
                case SHOVEL -> ToolEfficiency.MEDIUM;
                case SHEARS -> ToolEfficiency.NONE;
                case HAND -> ToolEfficiency.VERY_LOW;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 降低熵增
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加熵增量
            };
            case LIGHT -> switch (tool) {
                case PICKAXE -> ToolEfficiency.LOW;
                case AXE -> ToolEfficiency.NONE;
                case SHOVEL -> ToolEfficiency.NONE;
                case SHEARS -> ToolEfficiency.NONE;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 锁定光明
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加光明量
            };
            case SHADOW -> switch (tool) {
                case PICKAXE -> ToolEfficiency.LOW;
                case AXE -> ToolEfficiency.NONE;
                case SHOVEL -> ToolEfficiency.NONE;
                case SHEARS -> ToolEfficiency.LOW;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 保留暗影
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加暗影量
            };
            case MOTION -> switch (tool) {
                case PICKAXE -> ToolEfficiency.NONE;
                case AXE -> ToolEfficiency.NONE;
                case SHOVEL -> ToolEfficiency.LOW;
                case SHEARS -> ToolEfficiency.NONE;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.NONE;
                case FORTUNE -> ToolEfficiency.NONE;
            };
            case MIND -> switch (tool) {
                case PICKAXE -> ToolEfficiency.NONE;
                case AXE -> ToolEfficiency.NONE;
                case SHOVEL -> ToolEfficiency.NONE;
                case SHEARS -> ToolEfficiency.NONE;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 保留心灵
                case FORTUNE -> ToolEfficiency.ENHANCED;   // 增加心灵量
            };
            case SPACETIME -> switch (tool) {
                case PICKAXE -> ToolEfficiency.HIGH;
                case AXE -> ToolEfficiency.NONE;
                case SHOVEL -> ToolEfficiency.NONE;
                case SHEARS -> ToolEfficiency.NONE;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.SPECIAL; // 保留时空
                case FORTUNE -> ToolEfficiency.SPECIAL;    // 极小概率增幅
            };
            case RESONANCE -> switch (tool) {
                case PICKAXE -> ToolEfficiency.NONE;
                case AXE -> ToolEfficiency.LOW;
                case SHOVEL -> ToolEfficiency.NONE;
                case SHEARS -> ToolEfficiency.LOW;
                case HAND -> ToolEfficiency.NONE;
                case SILK_TOUCH -> ToolEfficiency.ENHANCED; // 增强共鸣
                case FORTUNE -> ToolEfficiency.ENHANCED;    // 增加共鸣量
            };
        };
    }
}
