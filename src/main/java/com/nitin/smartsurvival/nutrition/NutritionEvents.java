package com.nitin.smartsurvival.nutrition;

import com.nitin.smartsurvival.network.NutritionUpdatePayload;
import com.nitin.smartsurvival.smart_survival.Smart_survival;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/**
 * Event handlers for the nutrition system.
 * Handles food consumption and optional decay.
 */
public class NutritionEvents {

    private static long lastDecayTick = 0;

    /**
     * Register all nutrition-related events.
     * Call this from your mod initializer.
     */
    public static void register() {
        // Hook into item use callback - schedules processing after vanilla food logic
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }

            ItemStack stack = player.getItemInHand(hand);
            FoodProperties food = stack.get(DataComponents.FOOD);

            if (food == null || serverPlayer.isCreative()) {
                return InteractionResult.PASS;
            }

            // Schedule processing to happen AFTER vanilla eats the food
            if (level.getServer() != null) {
                level.getServer().execute(() -> {
                    processFood(serverPlayer, stack);
                });
            }

            return InteractionResult.PASS;
        });

        registerDecay();
    }

    /**
     * Process food consumption and apply nutrition/effects.
     */
    private static void processFood(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        FoodGroup group = FoodRegistry.get(stack.getItem());

        if (!group.isValid()) {
            Smart_survival.LOGGER.debug("Unregistered food: {}", FoodRegistry.getItemKey(stack.getItem()));
            return;
        }

        NutritionComponent comp = NutritionStorage.get(player);

        // Add nutrition
        int amount = NutritionRules.getGainForGroup(group);
        comp.add(group, amount);

        // Golden food bonus
        if (FoodRegistry.isGoldenFood(stack.getItem())) {
            for (FoodGroup g : FoodGroup.values()) {
                if (g.isValid() && g != group) {
                    comp.add(g, NutritionRules.GOLDEN_FOOD_BONUS);
                }
            }
        }

        applyEffects(player, comp);

        // Send nutrition update to client
        sendNutritionUpdate(player, comp);

        Smart_survival.LOGGER.debug("Player {} nutrition: {}", player.getName().getString(), comp);
    }

    /**
     * Register optional nutrition decay (every 5 minutes, -1 to all groups).
     */
    private static void registerDecay() {
        ServerTickEvents.END_WORLD_TICK.register((ServerLevel level) -> {
            long currentTick = level.getGameTime();

            if (currentTick - lastDecayTick < NutritionRules.DECAY_INTERVAL_TICKS) {
                return;
            }

            lastDecayTick = currentTick;

            for (var p : level.players()) {
                if (p instanceof ServerPlayer sp && !sp.isCreative()) {
                    NutritionComponent comp = NutritionStorage.get(sp);
                    comp.decayAll(NutritionRules.DECAY_AMOUNT);
                    // Send updated nutrition to client
                    sendNutritionUpdate(sp, comp);
                }
            }
        });
    }

    /**
     * Send nutrition data to a player's client.
     * @param player the player to send to
     * @param comp the nutrition component with current values
     */
    public static void sendNutritionUpdate(ServerPlayer player, NutritionComponent comp) {
        NutritionUpdatePayload payload = new NutritionUpdatePayload(
                comp.get(FoodGroup.PROTEIN),
                comp.get(FoodGroup.FIBER),
                comp.get(FoodGroup.SUGAR),
                comp.get(FoodGroup.FAT)
        );
        ServerPlayNetworking.send(player, payload);
    }

    /**
     * Apply status effects based on nutrition thresholds.
     * Uses one-effect-per-check rule to avoid spam.
     */
    public static void applyEffects(ServerPlayer player, NutritionComponent comp) {
        // Balanced Diet: Protein + Fiber >= threshold → Strength
        if (comp.get(FoodGroup.PROTEIN) >= NutritionRules.BALANCED_DIET_THRESHOLD
                && comp.get(FoodGroup.FIBER) >= NutritionRules.BALANCED_DIET_THRESHOLD) {
            if (!player.hasEffect(MobEffects.STRENGTH)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.STRENGTH,
                        NutritionRules.BALANCED_DIET_DURATION,
                        0,
                        false,
                        true,
                        true
                ));
            }
        }

        // Sugar Rush: High sugar → Speed
        if (comp.get(FoodGroup.SUGAR) >= NutritionRules.SUGAR_RUSH_THRESHOLD) {
            if (!player.hasEffect(MobEffects.SPEED)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.SPEED,
                        NutritionRules.SUGAR_RUSH_DURATION,
                        1,
                        false,
                        true,
                        true
                ));
            }
        }

        // Protein Tank: High protein → Damage Resistance
        if (comp.get(FoodGroup.PROTEIN) >= NutritionRules.PROTEIN_TANK_THRESHOLD) {
            if (!player.hasEffect(MobEffects.RESISTANCE)) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.RESISTANCE,
                        NutritionRules.PROTEIN_TANK_DURATION,
                        0,
                        false,
                        true,
                        true
                ));
            }
        }
    }
}



