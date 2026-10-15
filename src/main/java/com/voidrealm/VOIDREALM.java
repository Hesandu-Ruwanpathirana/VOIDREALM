
package com.voidrealm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VOIDREALM implements ModInitializer {
	public static final String MOD_ID = "voidrealm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(
				VoidBurstPayload.TYPE,
				VoidBurstPayload.CODEC
		);

		ServerPlayNetworking.registerGlobalReceiver(
				VoidBurstPayload.TYPE,
				(payload, context) -> context.server().execute(() -> {
					ServerPlayer player = context.player();

					// Find nearby living entities, excluding the activating player.
					for (LivingEntity target : player.level().getEntitiesOfClass(
							LivingEntity.class,
							player.getBoundingBox().inflate(4.0),
							entity -> entity != player
					)) {
						double dx = target.getX() - player.getX();
						double dz = target.getZ() - player.getZ();
						double distance = Math.sqrt(dx * dx + dz * dz);

						if (distance > 0.01) {
							target.push(
									(dx / distance) * 1.6,
									0.45,
									(dz / distance) * 1.6
							);
						}
					}
				})
		);

		LOGGER.info("VOIDREALM networking initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
