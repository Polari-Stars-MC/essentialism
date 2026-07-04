package com.essentialism.content.essence;

/**
 * Represents the concentration tier of an essence value.
 * <p>
 * Maps numeric essence values to descriptive tiers, as defined in the
 * design document's "Essence Concentration Scale".
 *
 * @see EssenceType
 */
public enum EssenceLevel {
    NONE(0, 0, "无", "无反馈"),
    TRACE(1, 9, "微", "单粒子，淡色，稀疏"),
    LOW(10, 29, "低", "少量粒子，有明显颜色"),
    MEDIUM(30, 59, "中", "颗粒状粒子流"),
    HIGH(60, 89, "高", "连续粒子流，带光晕"),
    EXTREME(90, 99, "极高", "浓密粒子束，强光晕"),
    INFINITE(100, Integer.MAX_VALUE, "无限", "爆发式粒子与持续辐射效果");

    private final int minValue;
    private final int maxValue;
    private final String chineseName;
    private final String particleDescription;

    EssenceLevel(int minValue, int maxValue, String chineseName, String particleDescription) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.chineseName = chineseName;
        this.particleDescription = particleDescription;
    }

    public int minValue() {
        return this.minValue;
    }

    public int maxValue() {
        return this.maxValue;
    }

    public String chineseName() {
        return this.chineseName;
    }

    public String particleDescription() {
        return this.particleDescription;
    }

    /**
     * Returns the concentration tier for a given essence value.
     *
     * @param value the essence concentration (as an integer)
     * @return the matching {@link EssenceLevel}, never null
     */
    public static EssenceLevel of(int value) {
        for (EssenceLevel level : values()) {
            if (value >= level.minValue && value <= level.maxValue) {
                return level;
            }
        }
        return INFINITE; // fallback — should not be reached
    }

    /**
     * Returns the concentration tier for a given float value,
     * rounding to the nearest integer first.
     */
    public static EssenceLevel of(float value) {
        return of(Math.round(value));
    }
}
