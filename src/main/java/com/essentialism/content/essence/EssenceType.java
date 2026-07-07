package com.essentialism.content.essence;

import net.minecraft.ChatFormatting;

import java.util.EnumMap;
import java.util.Map;

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
     * Returns the ARGB color int for UI rendering (bar charts, overlays).
     * Used by Screen classes for essence visualization.
     */
    public int argbColor() {
        return switch (this) {
            case SOLIDITY -> 0xFFAAAAAA;
            case LIFE -> 0xFF55FF55;
            case DECAY -> 0xFFAA0000;
            case LIGHT -> 0xFFFFAA00;
            case SHADOW -> 0xFFAA00AA;
            case MOTION -> 0xFF55FFFF;
            case MIND -> 0xFFFF55FF;
            case SPACETIME -> 0xFF5555FF;
            case RESONANCE -> 0xFFFFFF55;
        };
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
     * Returns the {@link com.essentialism.init.EItems EItems} solution entry
     * for this essence type. Used across multiple block entities and mechanics
     * to map an essence to its bottled form.
     */
    public dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry<?> solutionItem() {
        return switch (this) {
            case SOLIDITY -> com.essentialism.init.EItems.SOLIDITY_SOLUTION;
            case LIFE -> com.essentialism.init.EItems.LIFE_SOLUTION;
            case DECAY -> com.essentialism.init.EItems.DECAY_SOLUTION;
            case LIGHT -> com.essentialism.init.EItems.LIGHT_SOLUTION;
            case SHADOW -> com.essentialism.init.EItems.SHADOW_SOLUTION;
            case MOTION -> com.essentialism.init.EItems.MOTION_SOLUTION;
            case MIND -> com.essentialism.init.EItems.MIND_SOLUTION;
            case SPACETIME -> com.essentialism.init.EItems.SPACETIME_SOLUTION;
            case RESONANCE -> com.essentialism.init.EItems.RESONANCE_SOLUTION;
        };
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
     * Uses a pre-computed lookup table for O(1) access.
     */
    public ToolEfficiency toolEfficiency(ToolType tool) {
        return EFFICIENCY_TABLE.get(this).get(tool);
    }

    private static final Map<EssenceType, Map<ToolType, ToolEfficiency>> EFFICIENCY_TABLE;

    static {
        var table = new EnumMap<EssenceType, Map<ToolType, ToolEfficiency>>(EssenceType.class);
        for (var essence : EssenceType.values()) {
            table.put(essence, new EnumMap<>(ToolType.class));
        }

        // Helper to set a value
        var set = new Object() {
            void put(EssenceType e, ToolType t, ToolEfficiency eff) {
                table.get(e).put(t, eff);
            }
        };

        // ─── SOLIDITY ────────────────────────────────────────────────
        set.put(SOLIDITY, ToolType.PICKAXE, ToolEfficiency.HIGH);
        set.put(SOLIDITY, ToolType.AXE, ToolEfficiency.MEDIUM);
        set.put(SOLIDITY, ToolType.SHOVEL, ToolEfficiency.LOW);
        set.put(SOLIDITY, ToolType.SHEARS, ToolEfficiency.VERY_LOW);
        set.put(SOLIDITY, ToolType.HAND, ToolEfficiency.NONE);
        set.put(SOLIDITY, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(SOLIDITY, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── LIFE ────────────────────────────────────────────────────
        set.put(LIFE, ToolType.PICKAXE, ToolEfficiency.NONE);
        set.put(LIFE, ToolType.AXE, ToolEfficiency.HIGH);
        set.put(LIFE, ToolType.SHOVEL, ToolEfficiency.MEDIUM);
        set.put(LIFE, ToolType.SHEARS, ToolEfficiency.HIGH);
        set.put(LIFE, ToolType.HAND, ToolEfficiency.LOW);
        set.put(LIFE, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(LIFE, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── DECAY ───────────────────────────────────────────────────
        set.put(DECAY, ToolType.PICKAXE, ToolEfficiency.MEDIUM);
        set.put(DECAY, ToolType.AXE, ToolEfficiency.LOW);
        set.put(DECAY, ToolType.SHOVEL, ToolEfficiency.MEDIUM);
        set.put(DECAY, ToolType.SHEARS, ToolEfficiency.NONE);
        set.put(DECAY, ToolType.HAND, ToolEfficiency.VERY_LOW);
        set.put(DECAY, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(DECAY, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── LIGHT ───────────────────────────────────────────────────
        set.put(LIGHT, ToolType.PICKAXE, ToolEfficiency.LOW);
        set.put(LIGHT, ToolType.AXE, ToolEfficiency.NONE);
        set.put(LIGHT, ToolType.SHOVEL, ToolEfficiency.NONE);
        set.put(LIGHT, ToolType.SHEARS, ToolEfficiency.NONE);
        set.put(LIGHT, ToolType.HAND, ToolEfficiency.NONE);
        set.put(LIGHT, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(LIGHT, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── SHADOW ──────────────────────────────────────────────────
        set.put(SHADOW, ToolType.PICKAXE, ToolEfficiency.LOW);
        set.put(SHADOW, ToolType.AXE, ToolEfficiency.NONE);
        set.put(SHADOW, ToolType.SHOVEL, ToolEfficiency.NONE);
        set.put(SHADOW, ToolType.SHEARS, ToolEfficiency.LOW);
        set.put(SHADOW, ToolType.HAND, ToolEfficiency.NONE);
        set.put(SHADOW, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(SHADOW, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── MOTION ──────────────────────────────────────────────────
        set.put(MOTION, ToolType.PICKAXE, ToolEfficiency.NONE);
        set.put(MOTION, ToolType.AXE, ToolEfficiency.NONE);
        set.put(MOTION, ToolType.SHOVEL, ToolEfficiency.LOW);
        set.put(MOTION, ToolType.SHEARS, ToolEfficiency.NONE);
        set.put(MOTION, ToolType.HAND, ToolEfficiency.NONE);
        set.put(MOTION, ToolType.SILK_TOUCH, ToolEfficiency.NONE);
        set.put(MOTION, ToolType.FORTUNE, ToolEfficiency.NONE);

        // ─── MIND ────────────────────────────────────────────────────
        set.put(MIND, ToolType.PICKAXE, ToolEfficiency.NONE);
        set.put(MIND, ToolType.AXE, ToolEfficiency.NONE);
        set.put(MIND, ToolType.SHOVEL, ToolEfficiency.NONE);
        set.put(MIND, ToolType.SHEARS, ToolEfficiency.NONE);
        set.put(MIND, ToolType.HAND, ToolEfficiency.NONE);
        set.put(MIND, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(MIND, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        // ─── SPACETIME ───────────────────────────────────────────────
        set.put(SPACETIME, ToolType.PICKAXE, ToolEfficiency.HIGH);
        set.put(SPACETIME, ToolType.AXE, ToolEfficiency.NONE);
        set.put(SPACETIME, ToolType.SHOVEL, ToolEfficiency.NONE);
        set.put(SPACETIME, ToolType.SHEARS, ToolEfficiency.NONE);
        set.put(SPACETIME, ToolType.HAND, ToolEfficiency.NONE);
        set.put(SPACETIME, ToolType.SILK_TOUCH, ToolEfficiency.SPECIAL);
        set.put(SPACETIME, ToolType.FORTUNE, ToolEfficiency.SPECIAL);

        // ─── RESONANCE ───────────────────────────────────────────────
        set.put(RESONANCE, ToolType.PICKAXE, ToolEfficiency.NONE);
        set.put(RESONANCE, ToolType.AXE, ToolEfficiency.LOW);
        set.put(RESONANCE, ToolType.SHOVEL, ToolEfficiency.NONE);
        set.put(RESONANCE, ToolType.SHEARS, ToolEfficiency.LOW);
        set.put(RESONANCE, ToolType.HAND, ToolEfficiency.NONE);
        set.put(RESONANCE, ToolType.SILK_TOUCH, ToolEfficiency.ENHANCED);
        set.put(RESONANCE, ToolType.FORTUNE, ToolEfficiency.ENHANCED);

        EFFICIENCY_TABLE = table;
    }
}
