
package com.voidrealm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

public final class VoidBurstAnimation {

    private static final int TOTAL_TICKS = 70;
    private static final double TWO_PI = Math.PI * 2.0;

    private static boolean active = false;
    private static int animationTick = 0;

    private static double centerX;
    private static double centerY;
    private static double centerZ;

    private VoidBurstAnimation() {
    }

    public static void start(double x, double y, double z) {
        centerX = x;
        centerY = y;
        centerZ = z;

        animationTick = 0;
        active = true;
    }

    public static void tick(Minecraft client) {
        if (!active) {
            return;
        }

        if (client.player == null || client.level == null) {
            active = false;
            return;
        }

        animationTick++;

        // PHASE 1: CHARGE-UP (ticks 1-11)
        // A rotating ring of purple energy contracts inward.
        if (animationTick <= 11) {
            double progress = animationTick / 11.0;
            double radius = 3.2 - progress * 2.1;
            double height = 0.25 + progress * 0.8;
            int points = 26;

            for (int i = 0; i < points; i++) {
                double angle = TWO_PI * i / points
                        + animationTick * 0.16;

                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                client.level.addParticle(
                        ParticleTypes.PORTAL,
                        centerX + cos * radius,
                        centerY + height,
                        centerZ + sin * radius,
                        -cos * 0.04,
                        0.04,
                        -sin * 0.04
                );
            }

            if (animationTick % 2 == 0) {
                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        centerX,
                        centerY + 0.5 + progress,
                        centerZ,
                        0.0,
                        0.12,
                        0.0
                );
            }
        }

        // PHASE 2: VOID VORTEX (ticks 12-35)
        // Two twisting strands spiral upward around the player.
        else if (animationTick <= 35) {
            double progress = (animationTick - 11) / 24.0;
            int pointsPerStrand = 14;

            for (int strand = 0; strand < 2; strand++) {
                for (int i = 0; i < pointsPerStrand; i++) {
                    double verticalProgress =
                            i / (double) (pointsPerStrand - 1);

                    double angle = animationTick * 0.32
                            + verticalProgress * TWO_PI
                            + strand * Math.PI;

                    double radius = 0.7 + progress * 1.5;

                    double x = centerX + Math.cos(angle) * radius;
                    double y = centerY + 0.2
                            + verticalProgress * 2.6;
                    double z = centerZ + Math.sin(angle) * radius;

                    client.level.addParticle(
                            ParticleTypes.PORTAL,
                            x, y, z,
                            Math.cos(angle) * 0.035,
                            0.06,
                            Math.sin(angle) * 0.035
                    );
                }
            }

            // Bright energy climbing through the center.
            if (animationTick % 2 == 0) {
                double y = centerY + 0.4 + progress * 2.2;

                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        centerX,
                        y,
                        centerZ,
                        0.0,
                        0.16,
                        0.0
                );
            }
        }

        // PHASE 3: REALITY BREAK (ticks 36-56)
        // The flash begins as the expanding shockwave is released.
        else if (animationTick <= 56) {

            if (animationTick == 36) {
                VoidBurstScreenEffects.triggerRealityFlash();
            }

            double progress = (animationTick - 35) / 21.0;
            double radius = progress * 7.0;
            int points = 36;

            for (int i = 0; i < points; i++) {
                double angle = TWO_PI * i / points
                        + animationTick * 0.025;

                double cos = Math.cos(angle);
                double sin = Math.sin(angle);

                double x = centerX + cos * radius;
                double z = centerZ + sin * radius;

                client.level.addParticle(
                        ParticleTypes.PORTAL,
                        x,
                        centerY + 0.2,
                        z,
                        cos * 0.12,
                        0.035,
                        sin * 0.12
                );

                // A second, brighter ring adds depth to the shockwave.
                if (i % 2 == 0) {
                    client.level.addParticle(
                            ParticleTypes.END_ROD,
                            x,
                            centerY + 0.35,
                            z,
                            cos * 0.08,
                            0.06,
                            sin * 0.08
                    );
                }
            }
        }

        // PHASE 4: AFTERSHOCK (ticks 57-70)
        // The last sparks drift away from the center.
        else {
            int fadeTick = animationTick - 56;
            int sparks = 14;

            for (int i = 0; i < sparks; i++) {
                double angle = i * 2.399963229728653
                        + animationTick * 0.12;

                double radius = 0.8 + fadeTick * 0.22;

                double x = centerX + Math.cos(angle) * radius;
                double z = centerZ + Math.sin(angle) * radius;
                double y = centerY + 0.5 + (i % 5) * 0.28;

                client.level.addParticle(
                        ParticleTypes.PORTAL,
                        x, y, z,
                        Math.cos(angle) * 0.06,
                        0.08 + (i % 3) * 0.03,
                        Math.sin(angle) * 0.06
                );
            }
        }

        if (animationTick >= TOTAL_TICKS) {
            active = false;
        }
    }
}
