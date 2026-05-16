package com.clovercraftsmp.clover.mixin.feature.status;

import de.maxhenkel.status.playerstate.Availability;
import de.maxhenkel.status.playerstate.ClientPlayerStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerStateManager.class)
public abstract class ClientPlayerStateManagerMixin {
    @Shadow
    public abstract void setAvailability(Availability availability);

    @Shadow
    public abstract void setNoSleep(boolean noSleep);

    @Inject(method = "onConnect", at = @At("HEAD"))
    private void adjustStatusBeforeConnect(CallbackInfo ci) {
        this.setAvailability(Availability.OPEN);
        this.setNoSleep(false);
    }
}
