
package com.voidrealm.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.voidrealm.VOIDREALM;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
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
				if (client.player != null) {
					client.player.connection.sendChat("VOID BURST ACTIVATED!");
				}
			}
		});

		VOIDREALM.LOGGER.info("VOIDREALM power key registered!");
	}
}
