package com.clovercraftsmp.clover.mixin.feature.phantom;

import com.clovercraftsmp.clover.duck.PhantomDuck;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "handleDamageEvent", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtTime:I", opcode = Opcodes.PUTFIELD))
    private void onSetHurtTimeInDamageEvent(DamageSource damageSource, CallbackInfo ci) {
        if (!damageSource.is(DamageTypes.ON_FIRE) && this instanceof PhantomDuck duck) duck.clover$setNonFireHurtTime();
    }

    @Inject(method = "hurt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hurtTime:I", opcode = Opcodes.PUTFIELD))
    private void onSetHurtTimeInHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        if (!damageSource.is(DamageTypes.ON_FIRE) && this instanceof PhantomDuck duck) duck.clover$setNonFireHurtTime();
    }
}
