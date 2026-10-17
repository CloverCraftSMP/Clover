package com.clovercraftsmp.clover.mixin.feature.phantom;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomAttackPlayerTargetGoal")
public class PhantomAttackPlayerTargetGoalMixin {
    //? if >=26.1 {
    @WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;canAttack(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z"))
    private boolean addInsomniaCheck(Phantom instance, net.minecraft.server.level.ServerLevel level, LivingEntity target, TargetingConditions targetConditions, Operation<Boolean> original) {
        boolean canStart = original.call(instance, level, target, targetConditions);

        //noinspection resource
        if (instance.level().dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD) && target instanceof ServerPlayer serverPlayer) {
            int sleepTicks = Mth.clamp(serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            return (sleepTicks >= 72000 && canStart);
        }

        return canStart;
    }
    //? }

    //? if <=1.21.1 {
    /*@WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;canAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z"))
    private boolean addInsomniaCheck(Phantom instance, LivingEntity livingEntity, TargetingConditions targetingConditions, Operation<Boolean> original) {
        boolean canStart = original.call(instance, livingEntity, targetingConditions);

        if (instance.level().dimensionTypeRegistration().is(BuiltinDimensionTypes.OVERWORLD) && livingEntity instanceof ServerPlayer serverPlayer) {
            int sleepTicks = Mth.clamp(serverPlayer.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            return (sleepTicks >= 72000 && canStart);
        }

        return canStart;
    }
    *///? }
}
