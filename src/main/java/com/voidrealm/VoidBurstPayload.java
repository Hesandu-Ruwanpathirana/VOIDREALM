
package com.voidrealm;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record VoidBurstPayload() implements CustomPacketPayload {

    public static final Type<VoidBurstPayload> TYPE =
            new Type<>(VOIDREALM.id("void_burst"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VoidBurstPayload> CODEC =
            StreamCodec.unit(new VoidBurstPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
