package com.clovercraftsmp.clover.mixin.fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.emilsg.clutterbestiary.entity.custom.MossbloomEntity;
import net.minecraft.world.item.ShearsItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;

@IfModLoaded("clutterbestiary")
@Mixin(MossbloomEntity.class)
public abstract class MossbloomEntityMixin {
    @Shadow
    public abstract boolean getIsSaddled();

    @WrapOperation(method = "mobInteract", constant = @Constant(classValue = ShearsItem.class))
    private boolean wrapShears(Object object, Operation<Boolean> original) {
        return original.call(object) && this.getIsSaddled();
    }
}
