
package com.voidrealm.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.voidrealm.VOIDREALM;
import com.voidrealm.VoidBurstPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleTypes;
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
			if (client.player == null || client.level == null) {
				cooldownTicks[0] = 0;
				lastShownSecond[0] = -1;
				return;
			}

			// Countdown displayed in chat.
			if (cooldownTicks[0] > 0) {
				cooldownTicks[0]--;

				int secondsLeft = (cooldownTicks[0]
						+ TICKS_PER_SECOND - 1) / TICKS_PER_SECOND;

				if (secondsLeft != lastShownSecond[0]) {
					lastShownSecond[0] = secondsLeft;

					if (secondsLeft > 0) {
						client.player.sendSystemMessage(
								Component.literal(
										"§5✦ VOID BURST RECHARGING: §d"
												+ secondsLeft + "s"
								)
						);
					} else {
						client.player.sendSystemMessage(
								Component.literal(
										"§a✦ VOID BURST READY! Press G"
								)
						);
					}
				}
			}

			while (voidBurstKey.consumeClick()) {
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

				if (!ClientPlayNetworking.canSend(VoidBurstPayload.TYPE)) {
					client.player.sendSystemMessage(
							Component.literal(
									"§cVOIDREALM needs a compatible modded server!"
							)
					);
					continue;
				}

				// Request the server-side attack.
				ClientPlayNetworking.send(new VoidBurstPayload());

				cooldownTicks[0] = COOLDOWN_TICKS;
				lastShownSecond[0] = -1;

				client.player.sendSystemMessage(
						Component.literal("§5✦ VOID BURST ACTIVATED! ✦")
				);

				double cx = client.player.getX();
				double cy = client.player.getY();
				double cz = client.player.getZ();

				// Three large purple portal rings.
				double[] radii = {2.5, 4.5, 7.0};

				for (int ring = 0; ring < radii.length; ring++) {
					double radius = radii[ring];
					int count = 160;

					for (int i = 0; i < count; i++) {
						double angle = Math.PI * 2.0 * i / count;

						double x = cx + Math.cos(angle) * radius;
						double z = cz + Math.sin(angle) * radius;
						double y = cy + 0.2 + ring * 0.15;
						double speed = 0.2 + ring * 0.1;

						client.level.addParticle(
								ParticleTypes.PORTAL,
								x, y, z,
								Math.cos(angle) * speed,
								0.15 + ring * 0.04,
								Math.sin(angle) * speed
						);
					}
				}

				// Upward eruption of bright particles.
				for (int i = 0; i < 140; i++) {
					double angle = i * 2.399963229728653;
					double radius = 0.5 + (i % 12) * 0.2;
					double height = (i % 25) * 0.2;

					client.level.addParticle(
							ParticleTypes.END_ROD,
							cx + Math.cos(angle) * radius,
							cy + 0.5 + height,
							cz + Math.sin(angle) * radius,
							Math.cos(angle) * 0.12,
							0.25,
							Math.sin(angle) * 0.12
					);
				}

				// Dense central portal burst.
				for (int i = 0; i < 200; i++) {
					double angle = Math.PI * 2.0 * i / 200.0;
					double speed = 0.15 + (i % 10) * 0.08;

					client.level.addParticle(
							ParticleTypes.PORTAL,
							cx, cy + 1.0, cz,
							Math.cos(angle) * speed,
							0.2 + (i % 6) * 0.08,
							Math.sin(angle) * speed
					);
				}
			}
		});

		VOIDREALM.LOGGER.info("VOIDREALM power key registered!");
	}
}
