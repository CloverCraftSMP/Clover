package com.clovercraftsmp.clover.mixin.feature.copper_pipe;
//? if <=1.21.1 {
/*import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.lunade.copper.blocks.block_entity.CopperFittingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Objects;

@Mixin(CopperFittingEntity.class)
public class CopperFittingEntityMixin extends BlockEntity {
    public CopperFittingEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperFittingEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z"))
    private boolean wrapCanMoveIn(Level level, BlockPos pos, Direction direction, boolean _to, Operation<Boolean> original) {
        return ((PoweredDuck) this).clover$canTransferPoweredCheck(level, pos)
                && original.call(level, pos, direction, _to);
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperFittingEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Z"))
    private boolean wrapCanMoveOut(Level level, BlockPos pos, Direction direction, boolean _to, Operation<Boolean> original, @Share("otherPos") LocalRef<BlockPos> otherPos) {
        otherPos.set(pos);
        return ((PoweredDuck) this).clover$canTransferPoweredCheck(level, pos)
                && original.call(level, pos, direction, _to);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/storage/StorageView;isResourceBlank()Z"))
    private boolean addMoveInFilter(StorageView<ItemVariant> instance, Operation<Boolean> original) {
        return original.call(instance)
                || (((FilterDuck) this).clover$getFilter() != null
                && !Objects.requireNonNull(((FilterDuck) this).clover$getFilter()).test(instance.getResource().toStack()));
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/api/transfer/v1/storage/StorageView;isResourceBlank()Z"))
    private boolean addMoveOutFilter(StorageView<ItemVariant> instance, Operation<Boolean> original, @Share("otherPos") LocalRef<BlockPos> otherPos) {
        return original.call(instance)
                || (Objects.requireNonNull(this.level).getBlockEntity(otherPos.get()) instanceof FilterDuck filterDuck
                && filterDuck.clover$getFilter() != null
                && !Objects.requireNonNull(filterDuck.clover$getFilter()).test(instance.getResource().toStack()));
    }
}
*///?}