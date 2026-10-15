
package com.voidrealm;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VOIDREALM implements ModInitializer {
	public static final String MOD_ID = "voidrealm";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
	private static final long COOLDOWN_TICKS = 100;
	private static final double RADIUS = 4.0;
	private static final float DAMAGE = 6.0F;

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
					long currentTick = player.level().getGameTime();

					long readyAt = COOLDOWNS.getOrDefault(
							player.getUUID(), 0L
					);

					if (currentTick < readyAt) {
						player.sendSystemMessage(
								net.minecraft.network.chat.Component.literal(
										"§cVoid Burst is still recharging!"
								)
						);
						return;
					}

					COOLDOWNS.put(
							player.getUUID(),
							currentTick + COOLDOWN_TICKS
					);

					for (LivingEntity target : player.level().getEntitiesOfClass(
							LivingEntity.class,
							player.getBoundingBox().inflate(RADIUS),
							entity -> entity != player
					)) {
						double dx = target.getX() - player.getX();
						double dz = target.getZ() - player.getZ();
						double distance = Math.sqrt(dx * dx + dz * dz);

						// Damage nearby living entities on the server.
						target.hurtServer(
								player.level(),
								player.damageSources().playerAttack(player),
								DAMAGE
						);

						// Push targets outward from the player.
						if (distance > 0.01) {
							target.push(
									(dx / distance) * 1.6,
									0.45,
									(dz / distance) * 1.6
							);
						}
					}

					player.sendSystemMessage(
							net.minecraft.network.chat.Component.literal(
									"§5✦ VOID BURST! §7Cooldown: 5 seconds"
							)
					);
				})
		);

		LOGGER.info("VOIDREALM networking initialized!");
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
