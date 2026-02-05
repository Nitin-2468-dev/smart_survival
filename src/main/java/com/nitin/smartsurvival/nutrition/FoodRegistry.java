package com.nitin.smartsurvival.nutrition;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Registry mapping food items to their food groups.
 * Call init() during mod initialization.
 */
public class FoodRegistry {

    private static final Map<Item, FoodGroup> FOOD_MAP = new HashMap<>();

    /**
     * Initialize the food registry with all known food items.
     * Call this from your mod initializer.
     */
    public static void init() {
        // === PROTEIN (Meats) ===
        register(Items.COOKED_BEEF, FoodGroup.PROTEIN);
        register(Items.COOKED_PORKCHOP, FoodGroup.PROTEIN);
        register(Items.COOKED_CHICKEN, FoodGroup.PROTEIN);
        register(Items.COOKED_MUTTON, FoodGroup.PROTEIN);
        register(Items.COOKED_RABBIT, FoodGroup.PROTEIN);
        register(Items.COOKED_COD, FoodGroup.PROTEIN);
        register(Items.COOKED_SALMON, FoodGroup.PROTEIN);
        register(Items.BEEF, FoodGroup.PROTEIN);
        register(Items.PORKCHOP, FoodGroup.PROTEIN);
        register(Items.CHICKEN, FoodGroup.PROTEIN);
        register(Items.MUTTON, FoodGroup.PROTEIN);
        register(Items.RABBIT, FoodGroup.PROTEIN);
        register(Items.COD, FoodGroup.PROTEIN);
        register(Items.SALMON, FoodGroup.PROTEIN);
        register(Items.TROPICAL_FISH, FoodGroup.PROTEIN);
        register(Items.RABBIT_STEW, FoodGroup.PROTEIN);

        // === FIBER (Vegetables, Fruits, Grains) ===
        register(Items.CARROT, FoodGroup.FIBER);
        register(Items.GOLDEN_CARROT, FoodGroup.FIBER);
        register(Items.POTATO, FoodGroup.FIBER);
        register(Items.BAKED_POTATO, FoodGroup.FIBER);
        register(Items.BEETROOT, FoodGroup.FIBER);
        register(Items.BEETROOT_SOUP, FoodGroup.FIBER);
        register(Items.BREAD, FoodGroup.FIBER);
        register(Items.APPLE, FoodGroup.FIBER);
        register(Items.MELON_SLICE, FoodGroup.FIBER);
        register(Items.DRIED_KELP, FoodGroup.FIBER);
        register(Items.MUSHROOM_STEW, FoodGroup.FIBER);
        register(Items.SUSPICIOUS_STEW, FoodGroup.FIBER);

        // === SUGAR (Sweets, Berries) ===
        register(Items.SWEET_BERRIES, FoodGroup.SUGAR);
        register(Items.GLOW_BERRIES, FoodGroup.SUGAR);
        register(Items.HONEY_BOTTLE, FoodGroup.SUGAR);
        register(Items.COOKIE, FoodGroup.SUGAR);
        register(Items.PUMPKIN_PIE, FoodGroup.SUGAR);
        register(Items.CAKE, FoodGroup.SUGAR);
        register(Items.CHORUS_FRUIT, FoodGroup.SUGAR);

        // === FAT (Rich/Heavy Foods) ===
        register(Items.GOLDEN_APPLE, FoodGroup.FAT);
        register(Items.ENCHANTED_GOLDEN_APPLE, FoodGroup.FAT);
        register(Items.SPIDER_EYE, FoodGroup.FAT);
        register(Items.ROTTEN_FLESH, FoodGroup.FAT);
        register(Items.POISONOUS_POTATO, FoodGroup.FAT);
    }

    /**
     * Register a food item with a food group.
     * @param item the food item
     * @param group the food group
     */
    public static void register(Item item, FoodGroup group) {
        FOOD_MAP.put(item, group);
    }

    /**
     * Get the food group for an item.
     * @param item the food item
     * @return the food group, or UNKNOWN if not registered
     */
    public static FoodGroup get(Item item) {
        return FOOD_MAP.getOrDefault(item, FoodGroup.UNKNOWN);
    }

    /**
     * Get the food group as an Optional (for functional style).
     * @param item the food item
     * @return Optional containing the group, empty if UNKNOWN
     */
    public static Optional<FoodGroup> getOptional(Item item) {
        FoodGroup group = get(item);
        return group.isValid() ? Optional.of(group) : Optional.empty();
    }

    /**
     * Check if an item is registered in the food registry.
     * @param item the item to check
     * @return true if registered with a valid group
     */
    public static boolean isRegistered(Item item) {
        return FOOD_MAP.containsKey(item) && FOOD_MAP.get(item).isValid();
    }

    /**
     * Get the registry key for an item (for logging).
     * @param item the item
     * @return the registry key as string
     */
    public static String getItemKey(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    /**
     * Check if the item is a golden food (for bonus nutrition).
     * @param item the item to check
     * @return true if it's a golden food
     */
    public static boolean isGoldenFood(Item item) {
        return item == Items.GOLDEN_APPLE
                || item == Items.ENCHANTED_GOLDEN_APPLE
                || item == Items.GOLDEN_CARROT;
    }
}
