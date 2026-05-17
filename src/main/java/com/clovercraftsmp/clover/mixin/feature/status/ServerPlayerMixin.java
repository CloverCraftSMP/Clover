package com.clovercraftsmp.clover.mixin.feature.status;

import com.clovercraftsmp.clover.duck.StatusDuck;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.PlayerState;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("status")
@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements StatusDuck {
    @Unique private static final int AFK_TICKS = 20 * 60 * 2;
    @Unique private static final int NO_SLEEP_DURATION_TICKS = 20 * 60 * 20;

    @Unique private int ticksWithoutMovement = 0;
    @Unique private boolean wasAfk = false;
    @Unique private float lastYaw = 0.0f;
    @Unique private float lastPitch = 0.0f;
    @Unique private Vec3 lastPos = null;

    @Unique private int noSleepTicksRemaining = -1;

    @Override
    public void clover$receivedPlayerState(PlayerState state) {
        syncNoSleep(state.isNoSleep());
        syncAvailability(state.getAvailability());
    }

    @Unique
    private void syncNoSleep(boolean isNoSleep) {
        noSleepTicksRemaining = isNoSleep ? NO_SLEEP_DURATION_TICKS : -1;
    }

    @Unique
    private void syncAvailability(Availability availability) {
        if (availability == Availability.DO_NOT_DISTURB) return;

        boolean forceAfk = availability != Availability.OPEN;
        ticksWithoutMovement = forceAfk ? AFK_TICKS : 0;
        wasAfk = forceAfk;
    }

    @Unique
    private void tickAfk(ServerPlayer player) {
        float newYaw = player.getYHeadRot();
        float newPitch = player.getXRot();
        Vec3 newPos = player.position();

        boolean moved = newYaw != lastYaw || newPitch != lastPitch || !newPos.equals(lastPos);
        if (moved) ticksWithoutMovement = 0;
        else if (ticksWithoutMovement < AFK_TICKS) ticksWithoutMovement++;

        lastYaw = newYaw;
        lastPitch = newPitch;
        lastPos = newPos;

        boolean isAfk = ticksWithoutMovement >= AFK_TICKS;
        if (isAfk != wasAfk) {
            wasAfk = isAfk;
            ServerPlayNetworking.send(player, new ClientboundSetAfkPacket(isAfk));
        }
    }

    @Unique
    private void tickNoSleep(ServerPlayer player) {
        if (noSleepTicksRemaining < 0) return;
        if (noSleepTicksRemaining == 0) {
            ServerPlayNetworking.send(player, new ClientboundRemoveNoSleepPacket());
            noSleepTicksRemaining = -1;
            return;
        }
        noSleepTicksRemaining--;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        tickAfk(player);
        tickNoSleep(player);
    }
}
