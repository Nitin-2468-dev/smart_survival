package com.nitin.smartsurvival.nutrition;

/**
 * Central thresholds and constants for the nutrition system.
 * Adjust these values to balance gameplay.
 */
public final class NutritionRules {

    private NutritionRules() {} // Prevent instantiation

    // === Effect Thresholds ===
    /** Minimum protein + fiber for Strength effect */
    public static final int BALANCED_DIET_THRESHOLD = 50;

    /** Minimum sugar for Speed effect */
    public static final int SUGAR_RUSH_THRESHOLD = 80;

    /** Minimum protein for damage resistance */
    public static final int PROTEIN_TANK_THRESHOLD = 70;


    // === Nutrition Amounts Per Food Group ===
    public static final int PROTEIN_GAIN = 15;
    public static final int FIBER_GAIN = 10;
    public static final int SUGAR_GAIN = 8;
    public static final int FAT_GAIN = 12;

    // === Effect Durations (in ticks, 20 ticks = 1 second) ===
    public static final int BALANCED_DIET_DURATION = 20 * 30;  // 30 seconds
    public static final int SUGAR_RUSH_DURATION = 20 * 15;     // 15 seconds
    public static final int PROTEIN_TANK_DURATION = 20 * 20;   // 20 seconds

    // === Decay Settings ===
    /** How often decay ticks (in game ticks) - 5 minutes = 6000 ticks */
    public static final int DECAY_INTERVAL_TICKS = 6000;

    /** Amount to decay per interval */
    public static final int DECAY_AMOUNT = 1;

    // === Golden Food Bonus ===
    /** Extra nutrition added to all groups for golden foods */
    public static final int GOLDEN_FOOD_BONUS = 10;

    /**
     * Get the nutrition amount for a food group.
     * @param group the food group
     * @return the amount to add when eating
     */
    public static int getGainForGroup(FoodGroup group) {
        return switch (group) {
            case PROTEIN -> PROTEIN_GAIN;
            case FIBER -> FIBER_GAIN;
            case SUGAR -> SUGAR_GAIN;
            case FAT -> FAT_GAIN;
            case UNKNOWN -> 0;
        };
    }
}
