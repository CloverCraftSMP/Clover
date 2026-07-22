package com.clovercraftsmp.clover.mixin.fix.vanillabackport;

import com.blackgear.vanillabackport.common.api.leash.LeashIntegration;
import com.clovercraftsmp.clover.duck.LeashDuck;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded(value = "vanillabackport", maxVersion = "1.1.7.10", maxInclusive = false)
@Mixin(LeashIntegration.class)
public class LeashIntegrationMixin {
    @WrapOperation(method = "onInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Leashable;setLeashedTo(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void onSetLeashedTo(Leashable instance, Entity entity, boolean bl, Operation<Void> original) {
        ((LeashDuck) instance).clover$onLeashed();
        original.call(instance, entity, bl);
    }

    @Inject(method = "onInteract", at = @At("HEAD"), cancellable = true)
    private void preventUnleash(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        LeashDuck duck = (LeashDuck) entity;
        if (duck.clover$preventRemoveLeash()) cir.setReturnValue(InteractionResult.FAIL);
    }
}
