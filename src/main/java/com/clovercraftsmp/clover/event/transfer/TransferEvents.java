package com.clovercraftsmp.clover.event.transfer;

import com.clovercraftsmp.clover.transfer.TransferState;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class TransferEvents {
    private TransferEvents() {}

    public enum ArrivalKind { PUSHED_IN, RETURNED, REJOINED }

    public record Arrival(ArrivalKind kind, boolean sameSession, TransferState.Frame frame,
                          @Nullable TransferState.Frame current) {}

    public static final Event<ArrivedConfiguration> ARRIVED_CONFIGURATION = EventFactory.createArrayBacked(
            ArrivedConfiguration.class,
            callbacks -> (listener, arrival) -> {
                for (ArrivedConfiguration arrived : callbacks) {
                    arrived.onArrived(listener, arrival);
                }
            });

    public static final Event<ArrivedPlay> ARRIVED_PLAY = EventFactory.createArrayBacked(
            ArrivedPlay.class,
            callbacks -> (player, arrival) -> {
                for (ArrivedPlay arrived : callbacks) {
                    arrived.onArrived(player, arrival);
                }
            });

    @FunctionalInterface
    public interface ArrivedConfiguration {
        void onArrived(ServerConfigurationPacketListener listener, Arrival arrival);
    }

    @FunctionalInterface
    public interface ArrivedPlay {
        void onArrived(ServerPlayer player, Arrival arrival);
    }
}