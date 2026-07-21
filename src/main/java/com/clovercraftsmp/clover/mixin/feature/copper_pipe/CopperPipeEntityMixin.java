package com.clovercraftsmp.clover.mixin.feature.copper_pipe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.lunade.copper.blocks.CopperPipe;
import net.lunade.copper.blocks.block_entity.CopperPipeEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CopperPipeEntity.class)
public class CopperPipeEntityMixin extends BlockEntity {
    public CopperPipeEntityMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @WrapOperation(method = "moveIn", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZLnet/lunade/copper/blocks/block_entity/CopperPipeEntity;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)Z"))
    private boolean wrapMoveIn(Level level, BlockPos pos, boolean _to, @NotNull CopperPipeEntity copperPipe, @Nullable Storage<ItemVariant> inventory, @Nullable Storage<ItemVariant> pipeInventory, Operation<Boolean> original) {
        boolean powered = copperPipe.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(level, pos, _to, copperPipe, inventory, pipeInventory);
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canTransfer(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZLnet/lunade/copper/blocks/block_entity/CopperPipeEntity;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;Lnet/fabricmc/fabric/api/transfer/v1/storage/Storage;)Z"))
    private boolean wrapMoveOut(Level level, BlockPos pos, boolean _to, @NotNull CopperPipeEntity copperPipe, @Nullable Storage<ItemVariant> inventory, @Nullable Storage<ItemVariant> pipeInventory, Operation<Boolean> original) {
        boolean powered = copperPipe.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(level, pos, _to, copperPipe, inventory, pipeInventory);
    }

    @WrapOperation(method = "dispense", at = @At(value = "FIELD", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canDispense:Z", opcode = Opcodes.GETFIELD))
    private boolean wrapDispense(CopperPipeEntity instance, Operation<Boolean> original) {
        boolean powered = instance.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(instance);
    }

    @WrapOperation(method = "moveOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState wrapBlockState(Level instance, BlockPos blockPos, Operation<BlockState> original) {
        BlockState otherState = original.call(instance, blockPos);
        boolean shouldForceMoveOut = otherState.getBlock() instanceof CopperPipe && otherState.getValue(CopperPipe.POWERED);
        return shouldForceMoveOut ? Blocks.AIR.defaultBlockState() : otherState;
    }

    @WrapOperation(method = "dispenseMoveableNbt", at = @At(value = "FIELD", target = "Lnet/lunade/copper/blocks/block_entity/CopperPipeEntity;canDispense:Z", opcode = Opcodes.GETFIELD))
    private boolean wrapDispenseMovableNbt(CopperPipeEntity instance, Operation<Boolean> original) {
        boolean powered = instance.getBlockState().getValue(CopperPipe.POWERED);
        return !powered && original.call(instance);
    }
}
