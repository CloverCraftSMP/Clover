package com.clovercraftsmp.clover.mixin.feature.status;

import com.clovercraftsmp.clover.duck.StatusDuck;
import de.maxhenkel.status.net.PlayerStatePacket;
import de.maxhenkel.status.playerstate.PlayerStateManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerStateManager.class)
public class PlayerStateManagerMixin {
    @Inject(method = "lambda$new$0", at = @At("TAIL"))
    private void onUpdateFromPlayer(PlayerStatePacket packet, ServerPlayNetworking.Context context, CallbackInfo ci) {
        ((StatusDuck) context.player()).clover$receivedPlayerState(packet.getPlayerState());
    }
}
