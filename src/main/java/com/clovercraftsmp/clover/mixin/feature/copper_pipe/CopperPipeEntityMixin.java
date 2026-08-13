package com.clovercraftsmp.clover.mixin.feature.copper_pipe;

import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.lunade.copper.blocks.CopperFitting;
import net.lunade.copper.blocks.CopperPipe;
import net.lunade.copper.blocks.block_entity.CopperPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CopperPipeEntity.class)
public class CopperPipeEntityMixin extends BlockEntity implements PoweredDuck {
    public CopperPipeEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Override
    public boolean clover$isPowered() {
        return this.getBlockState().getValue(CopperPipe.POWERED);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZLnet/lunade/copper/blocks/block_entity/CopperPipeEntity;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)Z"))
    private boolean wrapCanMoveIn(Level level, BlockPos pos, boolean _to, @NotNull CopperPipeEntity copperPipe, @Nullable Storage<ItemVariant> inventory, @Nullable Storage<ItemVariant> pipeInventory, Operation<Boolean> original) {
        return preventTransferIfPowered(level, pos, _to, copperPipe, inventory, pipeInventory, original);
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZLnet/lunade/copper/blocks/block_entity/CopperPipeEntity;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)Z"))
    private boolean wrapCanMoveOut(Level level, BlockPos pos, boolean _to, @NotNull CopperPipeEntity copperPipe, @Nullable Storage<ItemVariant> inventory, @Nullable Storage<ItemVariant> pipeInventory, Operation<Boolean> original, @Share("otherPos") LocalRef<BlockPos> otherPos) {
        otherPos.set(pos);
        return preventTransferIfPowered(level, pos, _to, copperPipe, inventory, pipeInventory, original);
    }

    @Unique
    private boolean preventTransferIfPowered(Level level, BlockPos pos, boolean _to, @NotNull CopperPipeEntity copperPipe, @Nullable Storage<ItemVariant> inventory, @Nullable Storage<ItemVariant> pipeInventory, Operation<Boolean> original) {
        BlockState stateA = level.getBlockState(pos);
        boolean otherPowered = (stateA.getBlock() instanceof CopperPipe && stateA.getValue(CopperPipe.POWERED))
                || (stateA.getBlock() instanceof CopperFitting && stateA.getValue(CopperFitting.POWERED));
        boolean thisPowered = this.getBlockState().getValue(CopperPipe.POWERED);
        return !(thisPowered || otherPowered) && original.call(level, pos, _to, copperPipe, inventory, pipeInventory);
    }

    @WrapOperation(method = "dispense", at = @At(value = "FIELD", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canDispense:Z", opcode = Opcodes.GETFIELD))
    private boolean wrapDispense(CopperPipeEntity instance, Operation<Boolean> original) {
        boolean powered = instance.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(instance);
    }

    @WrapOperation(method = "dispenseMoveableNbt", at = @At(value = "FIELD", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canDispense:Z", opcode = Opcodes.GETFIELD))
    private boolean wrapDispenseMovableNbt(CopperPipeEntity instance, Operation<Boolean> original) {
        boolean powered = instance.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(instance);
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
