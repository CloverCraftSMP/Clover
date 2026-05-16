package com.clovercraftsmp.clover.networking;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundRemoveNoSleepPacket() implements CustomPacketPayload {
    public static final ResourceLocation REMOVE_NO_SLEEP = Clover.id("remove_no_sleep");
    public static final CustomPacketPayload.Type<ClientboundRemoveNoSleepPacket> TYPE = new CustomPacketPayload.Type<>(REMOVE_NO_SLEEP);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRemoveNoSleepPacket> CODEC = StreamCodec.unit(new ClientboundRemoveNoSleepPacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
