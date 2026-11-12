package com.clovercraftsmp.clover.transfer;

import com.clovercraftsmp.clover.Clover;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

//? if >=26.1
import net.minecraft.resources.Identifier;

//? if <=1.21.1
//import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class TransferUtil {
    public static final String COOKIE_PREFIX = "transfer_cookie_";

    //? if >=26.1
    public static final Identifier ROOT_COOKIE = Clover.id(COOKIE_PREFIX + "root");
    //? if <=1.21.1
    //public static final ResourceLocation ROOT_COOKIE = Clover.id(COOKIE_PREFIX + "root");

    private TransferUtil() {}

    public static CompletableFuture<Void> sendAway(ServerPlayer player, TransferState.Node target, byte[] extra) {
        if (!TransferConfig.isEnabled()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Transfer system is disabled"));
        }

        UUID uuid = player.getUUID();
        TransferState.Frame current = TransferRouter.current(uuid);
        TransferState.Session session = current != null
                ? current.session()
                : new TransferState.Session(TransferConfig.self().id(), UUID.randomUUID(), System.currentTimeMillis());
        int depth = current != null ? current.depth() + 1 : 1;

        TransferState.Frame frame = new TransferState.Frame(
                uuid, session, depth, UUID.randomUUID(), TransferConfig.self(), target, extra
        );

        return saveAndSend(player, TransferState.Kind.PUSH, frame, frames -> {
            if (current == null) frames.removeIf(x -> x.session().sameRootAs(session));
            frames.removeIf(x -> x.session().equals(session) && x.depth() >= depth);
            frames.add(new FrameStore.StoredFrame(frame, FrameStore.Role.SENT, false));
        });
    }

    public static CompletableFuture<Void> returnHome(ServerPlayer player, byte[] result) {
        if (!TransferConfig.isEnabled()) {
            return CompletableFuture.failedFuture(new IllegalStateException("Transfer system is disabled"));
        }

        TransferState.Frame current = TransferRouter.current(player.getUUID());
        if (current == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("No transfer session for " + player.getUUID()));
        }

        TransferState.Frame done = current.withExtra(result);
        return saveAndSend(player, TransferState.Kind.POP, done, frames -> {
            frames.removeIf(x -> x.session().equals(done.session())
                    && (x.depth() > done.depth() || (x.frameId().equals(done.frameId()) && x.role() == FrameStore.Role.RECEIVED)));
            frames.add(new FrameStore.StoredFrame(done, FrameStore.Role.RECEIVED, true));
        });
    }

    public static List<Packet<?>> buildPackets(TransferState.Kind kind, TransferState.Frame frame) {
        List<Packet<?>> packets = new ArrayList<>();

        packets.add(new ClientboundStoreCookiePacket(ROOT_COOKIE,
                frame.header(kind).toToken().getBytes(StandardCharsets.UTF_8)));

        List<String> sections = frame.sectionTokens();
        for (int i = 0; i < sections.size(); i++) {
            packets.add(new ClientboundStoreCookiePacket(
                    Clover.id(COOKIE_PREFIX + i),
                    sections.get(i).getBytes(StandardCharsets.UTF_8)));
        }

        TransferState.Node destination = frame.destination(kind);
        packets.add(new ClientboundTransferPacket(destination.host(), destination.port()));
        return packets;
    }

    private static CompletableFuture<Void> saveAndSend(ServerPlayer player, TransferState.Kind kind,
                                                       TransferState.Frame frame,
                                                       Consumer<List<FrameStore.StoredFrame>> edit) {
        List<Packet<?>> packets;
        try {
            packets = buildPackets(kind, frame);
        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }

        MinecraftServer server = player.level().getServer();
        return FrameStore.editAsync(frame.uuid(), edit).thenRunAsync(() -> {
            if (player.hasDisconnected()) return;
            packets.forEach(player.connection::send);
        }, server);
    }
}
