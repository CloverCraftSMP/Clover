package com.clovercraftsmp.clover.mixin.fix;

import com.li64.tide.data.item.TideItemData;
import com.li64.tide.registries.items.FishSatchelItem;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("tide")
@Mixin(value = Inventory.class)
public class InventoryMixin {
    @Shadow @Final public Player player;
    @Shadow @Final public NonNullList<ItemStack> items;

    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void reAddSatchelLogic(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (slot < 0 && FishSatchelItem.canPutInSatchel(stack)) {
            for (final ItemStack checked : this.items) {
                if (checked.getItem() instanceof FishSatchelItem satchelItem
                        && TideItemData.FISH_SATCHEL_OPENED.getOrDefault(checked, false)
                        && satchelItem.overrideOtherStackedOnMe(checked, stack, this.player, new SlotAccess() {
                    @NotNull
                    public ItemStack get() {
                        return checked;
                    }

                    public boolean set(ItemStack carried) {
                        return false;
                    }
                })) {
                    cir.setReturnValue(true);
                    break;
                }
            }
        }
    }
}
