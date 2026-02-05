package com.nitin.smartsurvival.handler;

import com.nitin.smartsurvival.smart_survival.Smart_survival;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class SleepHandler {

    private static final int DURATION = 6000;
    private static final int AMPLIFIER = 0;

    public static void register() {
        EntitySleepEvents.STOP_SLEEPING.register((entity, sleepingPos) -> {
            if (!(entity instanceof ServerPlayer player)) return;

            ServerLevel level = (ServerLevel) player.level();
            int comfortScore = calculateComfort(level, sleepingPos);

            if (comfortScore < 3) return;

            // Apply effect
            player.addEffect(new MobEffectInstance(
                    Smart_survival.WELL_RESTED,
                    DURATION,
                    AMPLIFIER
            ));

            // ONE clean message (localized later if you want)
            player.sendSystemMessage(
                    Component.literal("§6Well Rested: You recover health over time.")
            );

            // Particles
            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.FIREFLY,
                    player.getX(),
                    player.getY() + 1.5,
                    player.getZ(),
                    15,
                    0.5,
                    0.5,
                    0.5,
                    0.02
            );
        });
    }

    private static int calculateComfort(Level level, BlockPos bedPos) {
        int score = 0;

        for (BlockPos pos : BlockPos.betweenClosed(
                bedPos.offset(-2, -1, -2),
                bedPos.offset(2, 1, 2)
        )) {
            var state = level.getBlockState(pos);
            if (state.is(Blocks.LANTERN)
                    || state.is(Blocks.BOOKSHELF)
                    || state.is(Blocks.FLOWER_POT)) {
                score++;
            }
        }
        return score;
    }
}
