package com.clovercraftsmp.clover.mixin.fix;

import com.li64.tide.registries.items.FishSatchelItem;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("tide")
@Mixin(FishSatchelItem.class)
public class FishSatchelItemMixin {
    @WrapMethod(method = "overrideOtherStackedOnMe(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/SlotAccess;)Z")
    private boolean preventSatchelOverflowBug(ItemStack stack, ItemStack other, Player player, SlotAccess access, Operation<Boolean> original) {
        if (other.isEmpty()) return original.call(stack, other, player, access);
        int countBefore = other.getCount();
        boolean result = original.call(stack, other, player, access);
        return other.getCount() != countBefore && result;
    }
}