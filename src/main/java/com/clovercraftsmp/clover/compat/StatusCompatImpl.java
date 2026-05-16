package com.clovercraftsmp.clover.compat;

import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import de.maxhenkel.status.StatusClient;
import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.ClientPlayerStateManager;

public class StatusCompatImpl {
    public static void updateAfk(ClientboundSetAfkPacket packet) {
        ClientPlayerStateManager manager = StatusClient.STATE_MANAGER;
        boolean isDnd = manager.getAvailabilityIcon().getName().equals("do_not_disturb");
        if (!isDnd) manager.setAvailability(packet.afk() ? Availability.NONE : Availability.OPEN);
    }

    public static void removeNoSleep() {
        StatusClient.STATE_MANAGER.setNoSleep(false);
    }
}
