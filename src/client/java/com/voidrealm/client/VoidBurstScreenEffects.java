
package com.voidrealm.client;

import com.voidrealm.VOIDREALM;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class VoidBurstScreenEffects {

    private static final int CINEMATIC_DURATION_TICKS = 60;
    private static final int MAIN_FLASH_TICKS = 14;
    private static final int AFTERSHOCK_FLASH_TICKS = 5;

    private static int cinematicTick = -1;
    private static int flashTicks = 0;
    private static int flashDuration = 0;

    private static float flashStrength = 0.0f;
    private static boolean registered = false;

    private VoidBurstScreenEffects() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        // Draw above the normal HUD layers.
        HudElementRegistry.addLast(
                VOIDREALM.id("cinematic_overlay"),
                (graphics, deltaTracker) -> render(graphics)
        );
    }

    public static void startCinematicSequence() {
        cinematicTick = 0;
        flashTicks = 0;
        flashDuration = 0;
        flashStrength = 0.0f;
    }

    public static void triggerRealityFlash() {
        startFlash(MAIN_FLASH_TICKS, 1.0f);
    }

    public static void triggerAftershockPulse() {
        startFlash(AFTERSHOCK_FLASH_TICKS, 0.42f);
    }

    private static void startFlash(int duration, float strength) {
        flashTicks = duration;
        flashDuration = duration;
        flashStrength = strength;
    }

    public static void tick() {
        if (cinematicTick >= 0) {
            cinematicTick++;

            if (cinematicTick >= CINEMATIC_DURATION_TICKS) {
                cinematicTick = -1;
            }
        }

        if (flashTicks > 0) {
            flashTicks--;

            if (flashTicks == 0) {
                flashDuration = 0;
                flashStrength = 0.0f;
            }
        }
    }

    private static void render(GuiGraphicsExtractor graphics) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        if (width <= 0 || height <= 0) {
            return;
        }

        if (cinematicTick < 0 && flashTicks <= 0) {
            return;
        }

        // IMPACT FLASH: a strong violet wash that quickly fades.
        if (flashTicks > 0 && flashDuration > 0) {
            float progress = flashTicks / (float) flashDuration;
            float eased = progress * progress;

            int alpha = Math.min(
                    170,
                    Math.round(165.0f * eased * flashStrength)
            );

            int tint = (alpha << 24) | 0x006500D0;

            graphics.fill(0, 0, width, height, tint);

            // Reality-tear strips slide horizontally across the frame.
            if (flashStrength >= 0.8f) {
                renderGlitchBands(graphics, width, height, progress);
            }
        }

        // CINEMATIC LETTERBOX BARS.
        float barFraction = getBarFraction(cinematicTick);

        int barHeight = Math.round(height * barFraction);

        if (barHeight > 0) {
            // Deep black-purple bars.
            int barColor = 0xFF05010A;

            graphics.fill(
                    0, 0, width, Math.min(height, barHeight),
                    barColor
            );

            graphics.fill(
                    0, Math.max(0, height - barHeight),
                    width, height,
                    barColor
            );

            // Thin violet edges add a subtle energy outline.
            int edgeAlpha = Math.min(
                    220,
                    80 + Math.round(barFraction * 700.0f)
            );

            int edgeColor = (edgeAlpha << 24) | 0x009400FF;

            int topEdge = Math.min(height, barHeight + 1);
            int bottomEdge = Math.max(0, height - barHeight - 2);

            graphics.fill(
                    0, Math.max(0, topEdge - 2),
                    width, topEdge,
                    edgeColor
            );

            graphics.fill(
                    0, bottomEdge,
                    width, Math.min(height, bottomEdge + 2),
                    edgeColor
            );
        }

        // A short bright horizontal slash at the start of the main impact.
        if (flashTicks > 0
                && flashStrength >= 0.8f
                && flashTicks >= flashDuration - 3) {

            int lineY = height / 2;
            int lineColor = 0xBFFFFFFF;

            graphics.fill(
                    0, lineY - 1,
                    width, lineY + 2,
                    lineColor
            );
        }
    }

    private static void renderGlitchBands(
            GuiGraphicsExtractor graphics,
            int width,
            int height,
            float progress
    ) {
        int bandCount = 8;

        for (int i = 0; i < bandCount; i++) {
            int bandHeight = 2 + (i % 3) * 2;

            int bandWidth = Math.min(
                    width,
                    Math.max(
                            1,
                            width / 4 + (i % 4) * width / 10
                    )
            );

            int maxX = Math.max(1, width - bandWidth);
            int maxY = Math.max(1, height);

            int x = Math.floorMod(
                    flashTicks * 53 + i * 131,
                    maxX
            );

            int y = Math.floorMod(
                    flashTicks * 23 + i * 59,
                    maxY
            );

            int alpha = Math.min(
                    230,
                    95 + Math.round(120.0f * progress)
            );

            int rgb = i % 2 == 0
                    ? 0x00EB8CFF
                    : 0x0020004D;

            int color = (alpha << 24) | rgb;

            graphics.fill(
                    x,
                    y,
                    Math.min(width, x + bandWidth),
                    Math.min(height, y + bandHeight),
                    color
            );
        }
    }


    private static float getBarFraction(int tick) {
        if (tick < 0) {
            return 0.0f;
        }

        // Close in during the first part of the charge.
        if (tick <= 6) {
            return 0.105f * smooth(tick / 6.0f);
        }

        // Hold while the Void Gate forms.
        if (tick <= 29) {
            return 0.105f;
        }

        // Tighten the frame at Reality Break.
        if (tick <= 33) {
            float progress = smooth((tick - 29) / 4.0f);
            return 0.105f + 0.075f * progress;
        }

        // Hold during the main shockwave.
        if (tick <= 43) {
            return 0.18f;
        }

        // Open back up during the aftershock.
        if (tick <= 59) {
            float progress = smooth((tick - 43) / 16.0f);
            return 0.18f * (1.0f - progress);
        }

        return 0.0f;
    }


    private static float smooth(float value) {
        value = Math.max(0.0f, Math.min(1.0f, value));
        return value * value * (3.0f - 2.0f * value);
    }
}
