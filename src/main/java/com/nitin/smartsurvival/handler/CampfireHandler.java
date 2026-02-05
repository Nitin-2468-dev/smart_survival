package com.nitin.smartsurvival.handler;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;

public class CampfireHandler {
    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register((ServerLevel level) -> {
            if (level.getGameTime() % 20 == 0) {
                // Check if it's night time (13000 to 23000)
                long time = level.getDayTime() % 24000;
                boolean isNight = time > 13000 && time < 23000;

                for (Player player : level.players()) {
                    BlockPos playerPos = player.blockPosition();
                    if (level.isRainingAt(playerPos)) return;

                    if (isNearLitCampfire(level, playerPos)) {
                        // 1. Healing Logic (Always happens if campfire is lit)
                        if (isNight && player.getFoodData().getFoodLevel() > 6) {
                            player.heal(1.0f);
                        }


                        // 2. Particle Logic (ONLY happens at night)
                        if (isNight) {
                            spawnFireflies(level, playerPos, 12);
                        }
                    }
                }
            }
        });
    }

    private static boolean isNearLitCampfire(Level level, BlockPos pos) {
        // Radius set to 5 (as per your last edit)
        for (BlockPos currentPos : BlockPos.betweenClosed(pos.offset(-5, -4, -5), pos.offset(5, 4, 5))) {
            if (level.getBlockState(currentPos).is(Blocks.CAMPFIRE)) {
                if (level.getBlockState(currentPos).getValue(CampfireBlock.LIT)) {
                    if (!level.isRainingAt(currentPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static void spawnFireflies(ServerLevel level, BlockPos pos, int count) {
        level.sendParticles(
                ParticleTypes.FIREFLY,
                pos.getX() + 0.5,
                pos.getY() + 1.2,
                pos.getZ() + 0.5,
                count,
                1.2,
                0.6,
                1.2,
                0.02
        );
    }
}