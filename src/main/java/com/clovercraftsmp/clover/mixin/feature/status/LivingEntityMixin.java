package com.clovercraftsmp.clover.mixin.feature.status;

import de.maxhenkel.status.events.PlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "startSleeping", at = @At("TAIL"))
    private void onSleep(BlockPos blockPos, CallbackInfo ci) {
        if ((LivingEntity) (Object) this instanceof ServerPlayer serverPlayer) {
            PlayerEvents.PLAYER_SLEEP.invoker().accept(serverPlayer);
        }
    }
}
