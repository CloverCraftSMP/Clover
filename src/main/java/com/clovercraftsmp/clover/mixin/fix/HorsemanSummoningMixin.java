package com.clovercraftsmp.clover.mixin.fix;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.github.mortuusars.horseman.world.summoning.Summoning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("horseman")
@Mixin(Summoning.class)
public class HorsemanSummoningMixin {
    @Inject(
            method = "onHorseUnloaded",
            at = @At("HEAD"),
            cancellable = true
    )
    private void fixEntityDuplication(ServerLevel level, AbstractHorse horse, CallbackInfo ci) {
        if (horse.isRemoved()) ci.cancel();
    }
}
