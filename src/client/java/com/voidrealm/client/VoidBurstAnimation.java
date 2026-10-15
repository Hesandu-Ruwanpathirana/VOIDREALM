
package com.voidrealm.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;

public final class VoidBurstAnimation {

    private static final int TOTAL_TICKS = 60;
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

        VoidBurstScreenEffects.startCinematicSequence();
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

        // PHASE 1: COLLAPSING VOID FIELD (ticks 1-12).
        if (animationTick <= 8) {
            double progress = animationTick / 8.0;
            double outerRadius = 4.2 - progress * 2.7;
            double innerRadius = 2.5 - progress * 1.6;
            double rotation = animationTick * 0.17;

            addGroundRing(
                    client, outerRadius, 0.12, 26,
                    rotation, -0.075, 4
            );

            addGroundRing(
                    client, innerRadius, 0.28, 18,
                    -rotation * 1.4, -0.05, 3
            );

            if (animationTick % 2 == 0) {
                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        centerX,
                        centerY + 0.6 + progress,
                        centerZ,
                        0.0, 0.14, 0.0
                );
            }
        }

        // PHASE 2: THE VOID GATE (ticks 13-23).
        else if (animationTick <= 23) {
            double progress = (animationTick - 8) / 15.0;

            // Two spiraling strands climb through the gate.
            for (int strand = 0; strand < 2; strand++) {
                for (int i = 0; i < 8; i++) {
                    double verticalProgress = i / 7.0;

                    double angle = animationTick * 0.38
                            + verticalProgress * TWO_PI
                            + strand * Math.PI;

                    double radius = 0.75 + progress * 1.2;

                    double x = centerX + Math.cos(angle) * radius;
                    double y = centerY + 0.15
                            + verticalProgress * 3.0;
                    double z = centerZ + Math.sin(angle) * radius;

                    client.level.addParticle(
                            i % 3 == 0
                                    ? ParticleTypes.END_ROD
                                    : ParticleTypes.PORTAL,
                            x, y, z,
                            -Math.sin(angle) * 0.045,
                            0.075,
                            Math.cos(angle) * 0.045
                    );
                }
            }

            // Two intersecting vertical rings.
            if (animationTick % 2 == 0) {
                double rotation = animationTick * 0.085;

                addVerticalRing(
                        client, 1.55, 12, rotation, false
                );

                addVerticalRing(
                        client, 1.55, 12,
                        rotation + Math.PI / 2.0, true
                );
            }

            if (animationTick % 2 == 0) {
                client.level.addParticle(
                        ParticleTypes.END_ROD,
                        centerX,
                        centerY + 0.5 + progress * 2.0,
                        centerZ,
                        0.0, 0.2, 0.0
                );
            }
        }

        // PHASE 3: VOID ERUPTION (ticks 35-29).
        else if (animationTick <= 29) {
            double progress = (animationTick - 23) / 6.0;

            for (int i = 0; i < 26; i++) {
                double angle = TWO_PI * i / 26.0
                        + animationTick * 0.035;

                double speed = 0.18 + (i % 5) * 0.07;
                double verticalSpeed =
                        0.08 + ((i % 7) - 3) * 0.055;

                client.level.addParticle(
                        i % 4 == 0
                                ? ParticleTypes.END_ROD
                                : ParticleTypes.PORTAL,
                        centerX,
                        centerY + 1.2,
                        centerZ,
                        Math.cos(angle) * speed,
                        verticalSpeed,
                        Math.sin(angle) * speed
                );
            }

            addGroundRing(
                    client,
                    0.7 + progress * 1.8,
                    0.25,
                    18,
                    animationTick * 0.28,
                    0.14,
                    3
            );
        }

        // PHASE 4: REALITY BREAK (ticks 30-45).
        else if (animationTick <= 45) {
            if (animationTick == 30) {
                VoidBurstScreenEffects.triggerRealityFlash();
            }

            double progress = (animationTick - 29) / 16.0;
            double outerRadius = progress * 9.0;

            // Main expanding shockwave.
            addGroundRing(
                    client,
                    outerRadius,
                    0.18,
                    36,
                    animationTick * 0.025,
                    0.11,
                    4
            );

            // Second ring for a layered impact.
            if (animationTick % 2 == 0) {
                addGroundRing(
                        client,
                        outerRadius * 0.72,
                        0.42,
                        18,
                        -animationTick * 0.04,
                        0.075,
                        2
                );
            }

            // Brief rising rift during the initial impact.
            if (animationTick <= 34) {
                double riftRadius = 1.0
                        + (animationTick - 29) * 0.16;

                addVerticalRing(
                        client,
                        riftRadius,
                        12,
                        animationTick * 0.18,
                        false
                );
            }
        }

        // PHASE 5: AFTERSHOCK (ticks 46-88).
        else {
            if (animationTick == 46) {
                VoidBurstScreenEffects.triggerAftershockPulse();
            }

            int fadeTick = animationTick - 45;

            for (int i = 0; i < 12; i++) {
                double angle = i * 2.399963229728653
                        + animationTick * 0.09;

                double radius = 1.0 + fadeTick * 0.20;
                double height = 0.35 + (i % 6) * 0.42;

                double x = centerX + Math.cos(angle) * radius;
                double y = centerY + height;
                double z = centerZ + Math.sin(angle) * radius;

                client.level.addParticle(
                        i % 4 == 0
                                ? ParticleTypes.END_ROD
                                : ParticleTypes.PORTAL,
                        x, y, z,
                        Math.cos(angle) * 0.055,
                        0.055 + (i % 3) * 0.035,
                        Math.sin(angle) * 0.055
                );
            }
        }

        if (animationTick >= TOTAL_TICKS) {
            active = false;
        }
    }

    private static void addGroundRing(
            Minecraft client,
            double radius,
            double height,
            int points,
            double rotation,
            double radialSpeed,
            int brightEvery
    ) {
        for (int i = 0; i < points; i++) {
            double angle = TWO_PI * i / points + rotation;

            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            boolean bright = brightEvery > 0
                    && i % brightEvery == 0;

            client.level.addParticle(
                    bright
                            ? ParticleTypes.END_ROD
                            : ParticleTypes.PORTAL,
                    centerX + cos * radius,
                    centerY + height,
                    centerZ + sin * radius,
                    cos * radialSpeed,
                    bright ? 0.055 : 0.025,
                    sin * radialSpeed
            );
        }
    }

    private static void addVerticalRing(
            Minecraft client,
            double radius,
            int points,
            double rotation,
            boolean secondPlane
    ) {
        double ringCenterY = centerY + 1.55;

        for (int i = 0; i < points; i++) {
            double angle = TWO_PI * i / points + rotation;

            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            double x;
            double y = ringCenterY + sin * radius;
            double z;

            if (secondPlane) {
                x = centerX;
                z = centerZ + cos * radius;
            } else {
                x = centerX + cos * radius;
                z = centerZ;
            }

            client.level.addParticle(
                    i % 4 == 0
                            ? ParticleTypes.END_ROD
                            : ParticleTypes.PORTAL,
                    x, y, z,
                    secondPlane ? 0.0 : -sin * 0.025,
                    cos * 0.025,
                    secondPlane ? -sin * 0.025 : 0.0
            );
        }
    }
}
