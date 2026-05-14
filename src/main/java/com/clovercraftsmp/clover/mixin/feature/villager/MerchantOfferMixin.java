package com.clovercraftsmp.clover.mixin.feature.villager;

import com.clovercraftsmp.clover.util.ItemStackUtil;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin {
    @Shadow
    @Final
    private ItemStack result;

    @WrapMethod(method = "isOutOfStock")
    private boolean preventSellMending(Operation<Boolean> original) {
        return ItemStackUtil.hasMending(result) || original.call();
    }
}
