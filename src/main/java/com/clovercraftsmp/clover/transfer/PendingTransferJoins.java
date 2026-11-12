package com.clovercraftsmp.clover.transfer;

import com.clovercraftsmp.clover.event.transfer.TransferEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PendingTransferJoins {
    private static final ConcurrentHashMap<UUID, TransferEvents.Arrival> PENDING = new ConcurrentHashMap<>();

    private PendingTransferJoins() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            TransferEvents.Arrival arrival = PENDING.remove(player.getUUID());
            if (arrival != null) {
                TransferEvents.ARRIVED_PLAY.invoker().onArrived(player, arrival);
            }
        });
    }

    public static void put(UUID uuid, TransferEvents.Arrival arrival) {
        PENDING.put(uuid, arrival);
    }

    public static void clear(UUID uuid) {
        PENDING.remove(uuid);
    }
}
