package com.clovercraftsmp.clover.networking;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ClientboundSetAfkPacket(boolean afk) implements CustomPacketPayload {
    public static final ResourceLocation SET_AFK = Clover.id("set_afk");
    public static final CustomPacketPayload.Type<ClientboundSetAfkPacket> TYPE = new CustomPacketPayload.Type<>(SET_AFK);
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetAfkPacket> CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, ClientboundSetAfkPacket::afk, ClientboundSetAfkPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
