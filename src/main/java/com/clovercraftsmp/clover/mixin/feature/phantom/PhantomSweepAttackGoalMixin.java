package com.clovercraftsmp.clover.mixin.feature.phantom;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.duck.PhantomDuck;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Phantom;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@SuppressWarnings("resource")
@Mixin(targets = "net.minecraft.world.entity.monster.Phantom$PhantomSweepAttackGoal")
public class PhantomSweepAttackGoalMixin {
    @WrapOperation(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/monster/Phantom;hurtTime:I", opcode = Opcodes.GETFIELD))
    private int wrapHurtTime(Phantom instance, Operation<Integer> original) {
        return instance.level().getGameRules().getBoolean(Clover.PUNISH_INSOMNIACS) ?
                ((PhantomDuck) instance).clover$nonFireHurtTime() :
                original.call(instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Phantom;doHurtTarget(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean addFireDamage(Phantom instance, Entity entity, Operation<Boolean> original) {
        boolean success = original.call(instance, entity);

        if (success && instance.level().getGameRules().getBoolean(Clover.PUNISH_INSOMNIACS)) {
            float f = instance.level().getCurrentDifficultyAt(instance.blockPosition()).getEffectiveDifficulty();
            if (instance.isOnFire() && instance.getRandom().nextFloat() < f * 0.3) {
                entity.igniteForSeconds(2 * f);
            }
        }

        return success;
    }
}
