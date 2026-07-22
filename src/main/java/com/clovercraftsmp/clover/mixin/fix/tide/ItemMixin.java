package com.clovercraftsmp.clover.mixin.fix.tide;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;

//? if tide: >=2.1 {
import com.li64.tide.data.item.SatchelContents;
import com.li64.tide.data.item.TideDataComponents;
import com.li64.tide.data.item.TideItemData;
import com.li64.tide.registries.items.FishSatchelItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? }

@IfModLoaded(value = "tide", minVersion = "2.1")
@Mixin(Item.class)
public class ItemMixin {
    //? if tide: >=2.1 {
    @Inject(method = "verifyComponentsAfterLoad", at = @At("HEAD"))
    private void convertLegacySatchel(ItemStack stack, CallbackInfo ci) {
        if (!(stack.getItem() instanceof FishSatchelItem)) return;

        if (!stack.has(TideDataComponents.SATCHEL_CONTENTS) && stack.has(DataComponents.BUNDLE_CONTENTS)) {
            convertLegacySatchel(stack);
        }
    }

    @Unique
    private void convertLegacySatchel(ItemStack stack) {
        BundleContents legacy = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (legacy == null) return;

        SatchelContents.Mutable mutable = new SatchelContents.Mutable(new SatchelContents());
        for (ItemStack entry : legacy.items()) {
            for (int i = 0; i < entry.getCount(); i++) {
                mutable.tryInsert(entry.copyWithCount(1));
            }
        }

        TideItemData.SATCHEL_CONTENTS.set(stack, mutable.toImmutable());

        stack.remove(DataComponents.BUNDLE_CONTENTS);
        stack.remove(TideDataComponents.FISH_SATCHEL_OPENED);
    }
    //? }
}
