package com.clovercraftsmp.clover.mixin.fix.tide;

import com.li64.tide.registries.items.FishSatchelItem;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;

//? if tide: <2.1 {
/*import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
*///? }

@IfModLoaded(value = "tide", maxVersion = "2.1", maxInclusive = false)
@Mixin(FishSatchelItem.class)
public abstract class FishSatchelItemMixin {
    //? if tide: <2.1 {
    /*@WrapMethod(method = "overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z")
    private boolean preventSatchelOverflowBug(ItemStack stack, ItemStack other, Player player, SlotAccess access, Operation<Boolean> original) {
        if (other.isEmpty()) return original.call(stack, other, player, access);
        int countBefore = other.getCount();
        boolean result = original.call(stack, other, player, access);
        return other.getCount() != countBefore && result;
    }
    *///? }
}