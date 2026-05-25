package com.clovercraftsmp.clover.mixin.fix;

import com.li64.tide.registries.entities.misc.LootCrateEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("tide")
@Mixin(LootCrateEntity.class)
public class LootCrateEntityMixin {
    @WrapOperation(method = "create", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private static boolean preventSetBlock(Level instance, BlockPos blockPos, BlockState blockState, int i, Operation<Boolean> original) {
        return false;
    }
}
