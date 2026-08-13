package com.clovercraftsmp.clover.mixin.feature.copper_pipe;

import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.lunade.copper.blocks.CopperFitting;
import net.lunade.copper.blocks.CopperPipe;
import net.lunade.copper.blocks.block_entity.CopperFittingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CopperFittingEntity.class)
public class CopperFittingEntityMixin extends BlockEntity implements PoweredDuck {
    public CopperFittingEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public boolean clover$isPowered() {
        return this.getBlockState().getValue(CopperFitting.POWERED);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperFittingEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z"))
    private boolean wrapCanMoveIn(Level level, BlockPos pos, Direction direction, boolean _to, Operation<Boolean> original) {
        return preventTransferIfPowered(level, pos, direction, _to, original);
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperFittingEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z"))
    private boolean wrapCanMoveOut(Level level, BlockPos pos, Direction direction, boolean _to, Operation<Boolean> original, @Share("otherPos") LocalRef<BlockPos> otherPos) {
        otherPos.set(pos);
        return preventTransferIfPowered(level, pos, direction, _to, original);
    }

    @Unique
    private boolean preventTransferIfPowered(Level level, BlockPos pos, Direction direction, boolean _to, Operation<Boolean> original) {
        BlockState stateA = level.getBlockState(pos);
        boolean otherPowered = stateA.getBlock() instanceof CopperPipe && stateA.getValue(CopperPipe.POWERED) || stateA.getBlock() instanceof CopperFitting && stateA.getValue(CopperFitting.POWERED);
        boolean thisPowered = this.getBlockState().getValue(CopperPipe.POWERED);
        return !(thisPowered || otherPowered) && original.call(level, pos, direction, _to);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/storage/StorageView;isResourceBlank()Z"))
    private boolean addMoveInFilter(StorageView<ItemVariant> instance, Operation<Boolean> original) {
        if (original.call(instance)) return true;
        Filter filter = ((FilterDuck) this).clover$getFilter();
        if (filter == null) return false;
        return !filter.test(instance.getResource().toStack());
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/storage/StorageView;isResourceBlank()Z"))
    private boolean addMoveOutFilter(StorageView<ItemVariant> instance, Operation<Boolean> original, @Share("otherPos") LocalRef<BlockPos> otherPos) {
        if (original.call(instance)) return true;

        assert this.level != null;
        if (this.level.getBlockEntity(otherPos.get()) instanceof FilterDuck filterDuck) {
            Filter otherFilter = filterDuck.clover$getFilter();
            if (otherFilter != null) return !otherFilter.test(instance.getResource().toStack());
        }

        return false;
    }
}
