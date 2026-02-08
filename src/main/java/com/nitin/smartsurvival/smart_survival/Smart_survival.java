package com.nitin.smartsurvival.smart_survival;

import com.nitin.smartsurvival.handler.CampfireHandler;
import com.nitin.smartsurvival.handler.SleepHandler;
import com.nitin.smartsurvival.handler.TemperatureHandler; // ADD THIS
import com.nitin.smartsurvival.network.NutritionUpdatePayload;
import com.nitin.smartsurvival.nutrition.FoodRegistry;
import com.nitin.smartsurvival.nutrition.NutritionEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Smart_survival implements ModInitializer {
    public static final String MOD_ID = "smart_survival";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Holder<MobEffect> WELL_RESTED = Registry.registerForHolder(
            BuiltInRegistries.MOB_EFFECT,
            Identifier.fromNamespaceAndPath(MOD_ID, "well_rested"),
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFDF00) {

                @Override
                public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
                    if (entity.getHealth() < entity.getMaxHealth()) {
                        entity.heal(0.05f * (amplifier + 1));
                    }
                    return super.applyEffectTick(level, entity, amplifier);
                }

                @Override
                public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
                    return duration % 40 == 0;
                }

            }
    );

    @Override
    public void onInitialize() {
        LOGGER.info("Smart Survival: Booting...");

        // Initialize nutrition system
        FoodRegistry.init();
        NutritionEvents.register();
        NutritionUpdatePayload.register();

        // Other handlers
        CampfireHandler.register();
        SleepHandler.register();
        TemperatureHandler.register();

        LOGGER.info("Smart Survival: All systems initialized!");
    }
}