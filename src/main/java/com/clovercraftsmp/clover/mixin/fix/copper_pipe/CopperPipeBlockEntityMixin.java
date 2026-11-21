package com.clovercraftsmp.clover.mixin.fix.copper_pipe;
//? if >=26.1 {
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.lunade.copper.block.entity.CopperPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CopperPipeBlockEntity.class)
@IfModLoaded("simple_copper_pipes")
public class CopperPipeBlockEntityMixin {

    @Shadow public CopperPipeBlockEntity.DispenseType dispenseType;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void fixLitematicaCrash(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (this.dispenseType == null) this.dispenseType = CopperPipeBlockEntity.DispenseType.NONE;
    }
}
//? }