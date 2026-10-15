
package com.voidrealm.client;

import com.voidrealm.VOIDREALM;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class VoidBurstScreenEffects {

    private static final int FLASH_DURATION_TICKS = 8;

    private static int flashTicks = 0;
    private static boolean registered = false;

    private VoidBurstScreenEffects() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        HudElementRegistry.attachElementAfter(
                VanillaHudElements.MISC_OVERLAYS,
                VOIDREALM.id("reality_flash"),
                (graphics, deltaTracker) -> renderFlash(graphics)
        );
    }

    public static void triggerRealityFlash() {
        flashTicks = FLASH_DURATION_TICKS;
    }

    public static void tick() {
        if (flashTicks > 0) {
            flashTicks--;
        }
    }

    private static void renderFlash(GuiGraphicsExtractor graphics) {
        if (flashTicks <= 0) {
            return;
        }

        float progress = flashTicks / (float) FLASH_DURATION_TICKS;

        // Strongest at impact, then quickly fades.
        int alpha = Math.round(105.0f * progress * progress);

        // ARGB: translucent violet.
        int color = (alpha << 24) | 0x005700A8;

        graphics.fill(
                0,
                0,
                graphics.guiWidth(),
                graphics.guiHeight(),
                color
        );
    }
}
