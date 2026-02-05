package com.nitin.smartsurvival.smart_survival.client;

import com.nitin.smartsurvival.client.KeyInputHandler;
import com.nitin.smartsurvival.client.NutritionClientPacketHandler;
import net.fabricmc.api.ClientModInitializer;

public class Smart_survivalClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // Register key input handler for nutrition GUI (Z key)
        KeyInputHandler.register();

        // Register nutrition network packet handler to receive server updates
        NutritionClientPacketHandler.register();
    }
}





