package com.clovercraftsmp.clover.mixin.feature.copper_pipe;

import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lunade.copper.blocks.block_entity.AbstractSimpleCopperBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractSimpleCopperBlockEntity.class)
public abstract class AbstractSimpleCopperBlockEntityMixin implements FilterDuck, Container {
    @Unique
    private Filter filter;

    @Override
    public void clover$setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public @Nullable Filter clover$getFilter() {
        return filter;
    }

    @WrapOperation(method = "saveAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/AbstractSimpleCopperBlockEntity;trySaveLootTable(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private boolean addFilterToSave(AbstractSimpleCopperBlockEntity instance, CompoundTag tag, Operation<Boolean> original) {
        if (filter != null) tag.put(Filter.FILTER_PATH, filter.toCompound());
        return original.call(instance, tag);
    }

    @WrapOperation(method = "loadAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/AbstractSimpleCopperBlockEntity;tryLoadLootTable(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private boolean addFilterToLoad(AbstractSimpleCopperBlockEntity instance, CompoundTag tag, Operation<Boolean> original) {
        if (tag.contains(Filter.FILTER_PATH, 10)) filter = Filter.fromCompound(tag.getCompound(Filter.FILTER_PATH));
        return original.call(instance, tag);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void addFilterLoad(CompoundTag nbtCompound, HolderLookup.Provider lookupProvider, CallbackInfo ci) {
        if (nbtCompound.contains(Filter.FILTER_PATH)) {
            this.filter = Filter.fromCompound(nbtCompound.getCompound(Filter.FILTER_PATH));
        }
    }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return !((PoweredDuck) this).clover$isPowered() && (filter == null || filter.test(itemStack));
    }
}
