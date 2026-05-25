package com.clovercraftsmp.clover.mixin.fix.larion;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("larion")
@Mixin(value = NoiseBasedChunkGenerator.class, priority = 1500)
public abstract class ChunkGeneratorMixin {
    @TargetHandler(
            mixin = "com.badgerson.larion.mixin.NoiseChunkGeneratorMixin",
            name = "createFluidPicker",
            prefix = "handler"
    )
    @WrapMethod(
            method = "@MixinSquared:Handler"
    )
    private static void createFluidPicker(NoiseGeneratorSettings settings, CallbackInfoReturnable<Aquifer.FluidPicker> ci, Operation<Void> original) {
        if (settings.seaLevel() != 0) {
            original.call(settings, ci);
        }
    }
}
