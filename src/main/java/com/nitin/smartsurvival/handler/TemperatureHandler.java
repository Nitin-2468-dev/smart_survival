package com.nitin.smartsurvival.handler;

import com.nitin.smartsurvival.smart_survival.Smart_survival;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FurnaceBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemperatureHandler {

    private static final Map<UUID, Integer> playerTemperature = new HashMap<>();
    private static final Map<UUID, Integer> lastTemperature = new HashMap<>();
    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();

    // Temperature ranges
    private static final int TEMP_FREEZING = 10;      // Below this = freezing damage
    private static final int TEMP_COLD = 30;           // Cold zone
    private static final int TEMP_COMFORTABLE_MIN = 40; // Comfortable zone start
    private static final int TEMP_COMFORTABLE_MAX = 60; // Comfortable zone end
    private static final int TEMP_HOT = 70;            // Hot zone
    private static final int TEMP_BURNING = 90;        // Above this = heat damage

    // Heat source ranges
    private static final int CAMPFIRE_RANGE = 5;
    private static final int FURNACE_RANGE = 4;
    private static final int LAVA_RANGE = 4;
    private static final int FIRE_RANGE = 3;

    // Damage cooldown (10 seconds = 200 ticks)
    private static final long DAMAGE_COOLDOWN = 200;

    public static void register() {
        Smart_survival.LOGGER.info("TemperatureHandler: Registering...");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Check every second (20 ticks)
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    updatePlayerTemperature(player, server.getTickCount());
                }
            }
        });

        Smart_survival.LOGGER.info("TemperatureHandler: Registered successfully!");
    }

    private static void updatePlayerTemperature(ServerPlayer player, long currentTick) {
        UUID playerId = player.getUUID();
        BlockPos playerPos = player.blockPosition();
        ServerLevel level = (ServerLevel) player.level();

        int temperature = calculateTemperature(level, player, playerPos);

        // Store current temperature
        playerTemperature.put(playerId, temperature);

        // Get last temperature
        Integer lastTempObj = lastTemperature.get(playerId);
        int lastTemp = (lastTempObj == null) ? 50 : lastTempObj; // Start at comfortable temp

        // Show message if temperature changed significantly
        if (Math.abs(temperature - lastTemp) >= 15) {
            sendTemperatureMessage(player, temperature, lastTemp);
            lastTemperature.put(playerId, temperature);
        }

        // Apply temperature effects
        applyTemperatureEffects(player, temperature, currentTick);
    }

    private static int calculateTemperature(Level level, ServerPlayer player, BlockPos playerPos) {
        int temperature = 50; // Start at neutral temperature

        // === BIOME TEMPERATURE ===
        Biome biome = level.getBiome(playerPos).value();
        float biomeTemp = biome.getBaseTemperature();

        // Hot biomes (Desert, Badlands, Savanna, Nether)
        if (level.getBiome(playerPos).is(BiomeTags.IS_NETHER)) {
            temperature += 50; // Nether is extremely hot
        } else if (biomeTemp > 1.5f) {
            temperature += 30; // Very hot (Desert)
        } else if (biomeTemp > 1.0f) {
            temperature += 20; // Hot (Savanna, Badlands)
        }
        // Cold biomes (Taiga, Mountains, Ice)
        else if (biomeTemp < -0.5f) {
            temperature -= 40; // Extremely cold (Ice Spikes)
        } else if (biomeTemp < 0.0f) {
            temperature -= 30; // Very cold (Snowy biomes)
        } else if (biomeTemp < 0.3f) {
            temperature -= 20; // Cold (Taiga)
        }
        // Neutral biomes (Plains, Forest) - no change

        // === WEATHER ===
        if (level.isRaining()) {
            if (level.isThundering()) {
                temperature -= 15; // Heavy rain is colder
            } else {
                temperature -= 10; // Light rain
            }
        }

        // === TIME OF DAY ===
        long time = level.getDayTime() % 24000;
        if (time >= 13000 && time <= 23000) {
            temperature -= 10; // Night is colder
        }

        // === WATER ===
        if (player.isInWater()) {
            if (player.isUnderWater()) {
                temperature -= 40; // Underwater is very cold
            } else {
                temperature -= 25; // In water
            }
        }

        // === ALTITUDE ===
        int altitude = playerPos.getY();
        if (altitude > 100) {
            int altitudePenalty = (altitude - 100) / 10;
            temperature -= altitudePenalty;
        }

        // === HEAT SOURCES ===
        int heatFromSources = getHeatFromSources(level, playerPos);
        temperature += heatFromSources;

        // === COOLING ITEMS (in inventory) ===
        int coolingFromItems = getCoolingFromItems(player);
        temperature -= coolingFromItems;

        // Clamp between 0 and 100
        return Math.max(0, Math.min(100, temperature));
    }

    private static int getHeatFromSources(Level level, BlockPos playerPos) {
        int heat = 0;

        for (BlockPos currentPos : BlockPos.betweenClosed(
                playerPos.offset(-CAMPFIRE_RANGE, -2, -CAMPFIRE_RANGE),
                playerPos.offset(CAMPFIRE_RANGE, 2, CAMPFIRE_RANGE))) {

            double distance = Math.sqrt(playerPos.distSqr(currentPos));

            // Campfire (lit)
            if (level.getBlockState(currentPos).is(Blocks.CAMPFIRE)) {
                if (level.getBlockState(currentPos).getValue(CampfireBlock.LIT)) {
                    if (distance <= CAMPFIRE_RANGE) {
                        heat += (int)(30 * (1 - distance / CAMPFIRE_RANGE));
                    }
                }
            }

            // Soul Campfire (lit) - slightly cooler
            if (level.getBlockState(currentPos).is(Blocks.SOUL_CAMPFIRE)) {
                if (level.getBlockState(currentPos).getValue(CampfireBlock.LIT)) {
                    if (distance <= CAMPFIRE_RANGE) {
                        heat += (int)(20 * (1 - distance / CAMPFIRE_RANGE));
                    }
                }
            }

            // Furnace (lit)
            if (level.getBlockState(currentPos).is(Blocks.FURNACE) ||
                    level.getBlockState(currentPos).is(Blocks.BLAST_FURNACE) ||
                    level.getBlockState(currentPos).is(Blocks.SMOKER)) {
                if (level.getBlockState(currentPos).getValue(FurnaceBlock.LIT)) {
                    if (distance <= FURNACE_RANGE) {
                        heat += (int)(25 * (1 - distance / FURNACE_RANGE));
                    }
                }
            }

            // Lava
            if (level.getBlockState(currentPos).is(Blocks.LAVA)) {
                if (distance <= LAVA_RANGE) {
                    heat += (int)(50 * (1 - distance / LAVA_RANGE));
                }
            }

            // Fire
            if (level.getBlockState(currentPos).is(Blocks.FIRE)) {
                if (distance <= FIRE_RANGE) {
                    heat += (int)(25 * (1 - distance / FIRE_RANGE));
                }
            }

            // Magma Block
            if (level.getBlockState(currentPos).is(Blocks.MAGMA_BLOCK)) {
                if (distance <= 2) {
                    heat += (int)(15 * (1 - distance / 2));
                }
            }
        }

        return heat;
    }

    private static int getCoolingFromItems(ServerPlayer player) {
        int cooling = 0;

        // Access inventory items correctly for 1.21.1
        for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
            if (stack.is(Items.ICE)) {
                cooling += stack.getCount() * 2;
            } else if (stack.is(Items.PACKED_ICE)) {
                cooling += stack.getCount() * 3;
            } else if (stack.is(Items.BLUE_ICE)) {
                cooling += stack.getCount() * 4;
            } else if (stack.is(Items.POWDER_SNOW_BUCKET)) {
                cooling += stack.getCount() * 5;
            } else if (stack.is(Items.SNOWBALL)) {
                cooling += stack.getCount() / 4; // 4 snowballs = 1 cooling
            } else if (stack.is(Items.SNOW_BLOCK)) {
                cooling += stack.getCount() * 2;
            }
        }

        // Cap cooling from items
        return Math.min(cooling, 30);
    }

    private static void applyTemperatureEffects(ServerPlayer player, int temperature, long currentTick) {
        UUID playerId = player.getUUID();
        ServerLevel level = (ServerLevel) player.level();

        // FREEZING - Take damage
        if (temperature <= TEMP_FREEZING) {
            // Check if enough time has passed since last damage
            long lastDmg = lastDamageTime.getOrDefault(playerId, 0L);
            if (currentTick - lastDmg >= DAMAGE_COOLDOWN) {
                DamageSource freezeDamage = player.damageSources().freeze();
                player.hurt(freezeDamage, 2.0f);
                // Slowness I effect
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 0));
                lastDamageTime.put(playerId, currentTick);

                player.displayClientMessage(
                        Component.literal("❄ You are freezing to death!").withStyle(ChatFormatting.AQUA),
                        true
                );
            }
        }
        // COLD - Slowness effect
        else if (temperature <= TEMP_COLD) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, false, false));
        }
        // COMFORTABLE - No negative effects (regeneration would go here)
        else if (temperature >= TEMP_COMFORTABLE_MIN && temperature <= TEMP_COMFORTABLE_MAX) {
            // Optional: add regeneration or hunger reduction benefits
        }
        // HOT - Mining fatigue
        else if (temperature >= TEMP_HOT && temperature < TEMP_BURNING) {
            player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 0, false, false));
        }
        // BURNING - Take damage
        else if (temperature >= TEMP_BURNING) {
            long lastDmg = lastDamageTime.getOrDefault(playerId, 0L);
            if (currentTick - lastDmg >= DAMAGE_COOLDOWN) {
                player.setRemainingFireTicks(100);
                DamageSource fireDamage = player.damageSources().onFire();
                player.hurt(fireDamage, 2.0f);
                lastDamageTime.put(playerId, currentTick);

                player.displayClientMessage(
                        Component.literal("🔥 You are overheating!").withStyle(ChatFormatting.RED),
                        true
                );
            }
        }
    }

    private static void sendTemperatureMessage(ServerPlayer player, int currentTemp, int lastTemp) {
        Component message;

        if (currentTemp > lastTemp) {
            // Getting warmer
            if (currentTemp >= TEMP_BURNING) {
                message = Component.literal("🔥 Dangerously hot!").withStyle(ChatFormatting.DARK_RED);
            } else if (currentTemp >= TEMP_HOT) {
                message = Component.literal("Getting very hot...").withStyle(ChatFormatting.RED);
            } else if (currentTemp >= TEMP_COMFORTABLE_MAX) {
                message = Component.literal("Getting warmer...").withStyle(ChatFormatting.GOLD);
            } else {
                message = Component.literal("Warming up").withStyle(ChatFormatting.YELLOW);
            }
        } else {
            // Getting colder
            if (currentTemp <= TEMP_FREEZING) {
                message = Component.literal("❄ Dangerously frigid!").withStyle(ChatFormatting.DARK_AQUA);
            } else if (currentTemp <= TEMP_COLD) {
                message = Component.literal("Getting very cold...").withStyle(ChatFormatting.AQUA);
            } else if (currentTemp <= TEMP_COMFORTABLE_MIN) {
                message = Component.literal("Getting colder...").withStyle(ChatFormatting.BLUE);
            } else {
                message = Component.literal("Cooling down").withStyle(ChatFormatting.DARK_AQUA);
            }
        }

        player.displayClientMessage(message, true);
    }

    @SuppressWarnings("unused")
    public static int getTemperature(UUID playerId) {
        return playerTemperature.getOrDefault(playerId, 50);
    }
}