
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

	// Attack balance.
	private static final double RADIUS = 7.0;
	private static final float DAMAGE = 8.0F;

	// Knockback strength.
	private static final double HORIZONTAL_KNOCKBACK = 2.4;
	private static final double VERTICAL_KNOCKBACK = 0.85;

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

					int hitCount = 0;

					// Find all nearby living entities, not just one target.
					for (LivingEntity target :
							player.level().getEntitiesOfClass(
									LivingEntity.class,
									player.getBoundingBox().inflate(RADIUS),
									entity -> entity != player && entity.isAlive()
							)) {

						double dx = target.getX() - player.getX();
						double dy = target.getY() - player.getY();
						double dz = target.getZ() - player.getZ();

						// Enforce a spherical 3D attack radius.
						double distanceSquared =
								dx * dx + dy * dy + dz * dz;

						if (distanceSquared > RADIUS * RADIUS) {
							continue;
						}

						// Damage each target independently.
						target.hurtServer(
								player.level(),
								player.damageSources().playerAttack(player),
								DAMAGE
						);

						// Push horizontally away from the player.
						double horizontalDistance =
								Math.sqrt(dx * dx + dz * dz);

						double pushX;
						double pushZ;

						if (horizontalDistance > 0.01) {
							pushX = dx / horizontalDistance;
							pushZ = dz / horizontalDistance;
						} else {
							// Fallback for targets directly above or below
							// the player, where the horizontal direction
							// cannot be calculated from their positions.
							double yaw = Math.toRadians(player.getYRot());

							pushX = -Math.sin(yaw);
							pushZ = Math.cos(yaw);
						}

						target.push(
								pushX * HORIZONTAL_KNOCKBACK,
								VERTICAL_KNOCKBACK,
								pushZ * HORIZONTAL_KNOCKBACK
						);

						hitCount++;
					}

					player.sendSystemMessage(
							net.minecraft.network.chat.Component.literal(
									"§5✦ VOID BURST! §d"
											+ hitCount
											+ " targets hit! §7Cooldown: 5 seconds"
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
