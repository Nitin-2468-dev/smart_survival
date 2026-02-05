package com.nitin.smartsurvival.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Handles key input for the nutrition system.
 * Registers "Z" key to open the nutrition screen.
 */
public class KeyInputHandler {

    public static final String KEY_NUTRITION = "key.smart_survival.nutrition";

    public static KeyMapping NUTRITION_KEY;

    public static void register() {
        // Register the Z key binding using Category.MISC (for custom mod keys)
        NUTRITION_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_NUTRITION,
                GLFW.GLFW_KEY_Z,
                KeyMapping.Category.MISC
        ));

        // Listen for key press each tick
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (NUTRITION_KEY.consumeClick()) {
                openNutritionScreen(client);
            }
        });
    }

    private static void openNutritionScreen(Minecraft client) {
        if (client.player != null && client.screen == null) {
            client.setScreen(new NutritionScreen());
        }
    }
}
