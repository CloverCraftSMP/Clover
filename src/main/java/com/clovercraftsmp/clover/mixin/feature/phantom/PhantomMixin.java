package com.clovercraftsmp.clover.mixin.feature.phantom;

import com.clovercraftsmp.clover.duck.PhantomDuck;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Phantom.class)
public class PhantomMixin implements PhantomDuck {
    @Unique
    private int nonFireHurtTime = 0;

    @Override
    public int clover$nonFireHurtTime() {
        return nonFireHurtTime;
    }

    @Override
    public void clover$setNonFireHurtTime() {
        this.nonFireHurtTime = 10;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (nonFireHurtTime > 0) nonFireHurtTime--;
    }
}
