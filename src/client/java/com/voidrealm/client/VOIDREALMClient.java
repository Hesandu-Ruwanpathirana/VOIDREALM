
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

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (voidBurstKey.consumeClick()) {
				if (client.player == null || client.level == null) {
					continue;
				}

				// Ask the server to perform the real knockback.
				if (ClientPlayNetworking.canSend(VoidBurstPayload.TYPE)) {
					ClientPlayNetworking.send(new VoidBurstPayload());
				} else {
					client.player.sendSystemMessage(
							Component.literal(
									"VOIDREALM needs a compatible modded server!"
							)
					);
					continue;
				}

				// Local visual effect: purple portal particles in a ring.
				client.player.sendSystemMessage(
						Component.literal("§5✦ VOID BURST ACTIVATED! ✦")
				);

				for (int i = 0; i < 100; i++) {
					double angle = (Math.PI * 2 * i) / 100;
					double radius = 3.0;

					double x = client.player.getX()
							+ Math.cos(angle) * radius;
					double y = client.player.getY() + 1.0;
					double z = client.player.getZ()
							+ Math.sin(angle) * radius;

					client.level.addParticle(
							ParticleTypes.PORTAL,
							x, y, z,
							Math.cos(angle) * 0.25,
							0.12,
							Math.sin(angle) * 0.25
					);
				}
			}
		});

		VOIDREALM.LOGGER.info("VOIDREALM power key registered!");
	}
}
