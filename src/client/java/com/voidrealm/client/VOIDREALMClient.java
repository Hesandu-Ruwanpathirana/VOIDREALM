
package com.voidrealm.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.voidrealm.VOIDREALM;
import com.voidrealm.VoidBurstPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class VOIDREALMClient implements ClientModInitializer {

	private static final int COOLDOWN_TICKS = 100;
	private static final int TICKS_PER_SECOND = 20;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(
				VOIDREALM.id("powers")
		);

		KeyMapping voidBurstKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping(
						"key.voidrealm.void_burst",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_G,
						category
				)
		);

		final int[] cooldownTicks = {0};
		final int[] lastShownSecond = {-1};

		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			// Keep the Phase 3 animation moving every client tick.
			VoidBurstAnimation.tick(client);

			if (client.player == null || client.level == null) {
				cooldownTicks[0] = 0;
				lastShownSecond[0] = -1;
				return;
			}

			// Show the remaining cooldown as an on-screen overlay.
			if (cooldownTicks[0] > 0) {
				cooldownTicks[0]--;

				int secondsLeft = (cooldownTicks[0]
						+ TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;

				if (secondsLeft != lastShownSecond[0]) {
					lastShownSecond[0] = secondsLeft;

					if (secondsLeft > 0) {
						client.player.sendOverlayMessage(
								Component.literal(
										"§5✦ VOID BURST RECHARGING: §d"
												+ secondsLeft + "s"
								)
						);
					} else {
						client.player.sendOverlayMessage(
								Component.literal(
										"§a✦ VOID BURST READY! Press G"
								)
						);
					}
				}
			}

			while (voidBurstKey.consumeClick()) {

				// Stop activation while the cooldown is running.
				if (cooldownTicks[0] > 0) {
					int secondsLeft = (cooldownTicks[0]
							+ TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;

					client.player.sendSystemMessage(
							Component.literal(
									"§cVoid Burst recharging: "
											+ secondsLeft + "s"
							)
					);
					continue;
				}

				// Make sure the server supports the ability payload.
				if (!ClientPlayNetworking.canSend(VoidBurstPayload.TYPE)) {
					client.player.sendSystemMessage(
							Component.literal(
									"§cVOIDREALM needs a compatible modded server!"
							)
					);
					continue;
				}

				// Ask the server to perform the attack.
				ClientPlayNetworking.send(new VoidBurstPayload());

				// Start the local cooldown.
				cooldownTicks[0] = COOLDOWN_TICKS;
				lastShownSecond[0] = -1;

				client.player.sendSystemMessage(
						Component.literal("§5✦ VOID BURST ACTIVATED! ✦")
				);

				// PHASE 3: Start the timed animation sequence.
				// Charge-up -> vortex -> shockwave -> fading sparks.
				VoidBurstAnimation.start(
						client.player.getX(),
						client.player.getY(),
						client.player.getZ()
				);
			}
		});

		VOIDREALM.LOGGER.info("VOIDREALM power key registered!");
	}
}
