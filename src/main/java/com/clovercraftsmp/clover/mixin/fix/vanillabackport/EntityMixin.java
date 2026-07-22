package com.clovercraftsmp.clover.mixin.fix.vanillabackport;

import com.clovercraftsmp.clover.duck.LeashDuck;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded(value = "vanillabackport", maxVersion = "1.1.7.10", maxInclusive = false)
@Mixin(Entity.class)
public class EntityMixin implements LeashDuck {
    @Unique private int leashedCounter = 0;

    @Override
    public boolean clover$preventRemoveLeash() {
        return leashedCounter > 0;
    }

    @Override
    public void clover$onLeashed() {
        leashedCounter = 2;
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void afterTick(CallbackInfo ci) {
        if (leashedCounter > 0) leashedCounter--;
    }
}
