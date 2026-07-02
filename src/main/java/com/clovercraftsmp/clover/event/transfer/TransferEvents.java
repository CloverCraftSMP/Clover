package com.clovercraftsmp.clover.event.transfer;

import com.clovercraftsmp.clover.util.DataBaseUtil.PlayerState;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class TransferEvents {
    private TransferEvents() {}

    public static final Event<ReceivedTransferConfiguration> RECEIVED_CONFIGURATION = EventFactory.createArrayBacked(ReceivedTransferConfiguration.class, callbacks -> (listener, cookieState, localState) -> {        for (ReceivedTransferConfiguration received : callbacks) {
            received.onReceived(listener, cookieState, localState);
        }
    });

    public static final Event<ReceivedTransferPlay> RECEIVED_PLAY = EventFactory.createArrayBacked(ReceivedTransferPlay.class, callbacks -> (player, cookieState, localState) -> {
        for (ReceivedTransferPlay received : callbacks) {
            received.onReceived(player, cookieState, localState);
        }
    });

    @FunctionalInterface
    public interface ReceivedTransferConfiguration {
        void onReceived(ServerConfigurationPacketListener listener, PlayerState cookieState, @Nullable PlayerState localState);
    }

    @FunctionalInterface
    public interface ReceivedTransferPlay {
        void onReceived(ServerPlayer player, PlayerState cookieState, @Nullable PlayerState localState);
    }
}
