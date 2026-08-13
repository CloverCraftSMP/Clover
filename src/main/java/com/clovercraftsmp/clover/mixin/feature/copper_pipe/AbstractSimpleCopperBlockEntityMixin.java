package com.clovercraftsmp.clover.mixin.feature.copper_pipe;

import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.lunade.copper.blocks.CopperFitting;
import net.lunade.copper.blocks.CopperPipe;
import net.lunade.copper.blocks.block_entity.AbstractSimpleCopperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractSimpleCopperBlockEntity.class)
public abstract class AbstractSimpleCopperBlockEntityMixin extends BlockEntity implements FilterDuck, PoweredDuck, Container {
    public AbstractSimpleCopperBlockEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public boolean clover$canTransferPoweredCheck(Level level, BlockPos pos) {
        boolean thisPowered = this.getBlockState().getValue(BlockStateProperties.POWERED);

        BlockState otherState = level.getBlockState(pos);
        boolean otherPowered =
                (otherState.getBlock() instanceof CopperFitting || otherState.getBlock() instanceof CopperPipe)
                && otherState.getValue(BlockStateProperties.POWERED);

        return !thisPowered && !otherPowered;
    }

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

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return !this.getBlockState().getValue(BlockStateProperties.POWERED)
                && (filter == null || filter.test(itemStack));
    }
}
