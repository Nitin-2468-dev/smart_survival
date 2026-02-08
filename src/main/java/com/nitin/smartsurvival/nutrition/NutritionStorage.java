package com.nitin.smartsurvival.nutrition;

import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Static storage for player nutrition data.
 * Note: This is volatile - data is lost on server restart.
 * For persistence, consider Cardinal Components or player NBT later.
 */
public class NutritionStorage {

    private static final Map<UUID, NutritionComponent> PLAYERS = new HashMap<>();

    /**
     * Get or create nutrition data for a player.
     * @param player the server player
     * @return the player's nutrition component
     */
    public static NutritionComponent get(ServerPlayer player) {
        return PLAYERS.computeIfAbsent(player.getUUID(), uuid -> new NutritionComponent());
    }

    /**
     * Get nutrition data by UUID (for offline access if needed).
     * @param uuid the player's UUID
     * @return the nutrition component, or null if not found
     */
    public static NutritionComponent getByUUID(UUID uuid) {
        return PLAYERS.get(uuid);
    }

    /**
     * Remove a player's nutrition data (e.g., on disconnect cleanup).
     * @param player the server player
     */
    public static void remove(ServerPlayer player) {
        PLAYERS.remove(player.getUUID());
    }

    /**
     * Clear all stored nutrition data.
     */
    public static void clear() {
        PLAYERS.clear();
    }

    /**
     * Get the number of tracked players.
     * @return player count
     */
    public static int size() {
        return PLAYERS.size();
    }
}
