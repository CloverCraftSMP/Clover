package com.clovercraftsmp.clover.mixin.feature.copper_pipe;
import com.clovercraftsmp.clover.duck.FilterDuck;
import com.clovercraftsmp.clover.duck.PoweredDuck;
import com.clovercraftsmp.clover.util.filter.Filter;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//? if >=26.1 {
import net.lunade.copper.block.CopperFittingBlock;
import net.lunade.copper.block.CopperPipeBlock;
import net.lunade.copper.block.entity.AbstractSimpleCopperBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//?} else {
/*import net.lunade.copper.blocks.CopperFitting;
import net.lunade.copper.blocks.CopperPipe;
import net.lunade.copper.blocks.block_entity.AbstractSimpleCopperBlockEntity;
*///?}
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
        Block otherBlock = otherState.getBlock();

        //? if <=1.21.1 {
        /*boolean otherIsCopperPipe = otherBlock instanceof CopperFitting || otherBlock instanceof CopperPipe;
        *///? }

        //? if >=26.1 {
        boolean otherIsCopperPipe = otherBlock instanceof CopperFittingBlock || otherBlock instanceof CopperPipeBlock;
        //? }

        boolean otherPowered = otherIsCopperPipe && otherState.getValue(BlockStateProperties.POWERED);

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

    //? if <=1.21.1 {
    /*@WrapOperation(method = "saveAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/AbstractSimpleCopperBlockEntity;trySaveLootTable(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private boolean addFilterToSave(AbstractSimpleCopperBlockEntity instance, CompoundTag tag, Operation<Boolean> original) {
        if (filter != null) tag.put(Filter.FILTER_PATH, filter.toCompound());
        return original.call(instance, tag);
    }
    *///? }

    //? if >=26.1 {
    @WrapOperation(method = "saveAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/block/entity/AbstractSimpleCopperBlockEntity;trySaveLootTable(Lnet/minecraft/world/level/storage/ValueOutput;)Z"))
    private boolean addFilterToSave(AbstractSimpleCopperBlockEntity instance, ValueOutput output, Operation<Boolean> original) {
        if (filter != null) output.store(Filter.FILTER_PATH, CompoundTag.CODEC, filter.toCompound());
        return original.call(instance, output);
    }
    //? }

    //? if <=1.21.1 {
    /*@WrapOperation(method = "loadAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/blocks/block_entity/AbstractSimpleCopperBlockEntity;tryLoadLootTable(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private boolean addFilterToaLoad1(AbstractSimpleCopperBlockEntity instance, CompoundTag tag, Operation<Boolean> original) {
        if (tag.contains(Filter.FILTER_PATH, 10)) filter = Filter.fromCompound(tag.getCompound(Filter.FILTER_PATH));
        return original.call(instance, tag);
    }
    *///? }

    //? if >=26.1 {
    @WrapOperation(method = "loadAdditional", at = @At(value = "INVOKE", target = "Lnet/lunade/copper/block/entity/AbstractSimpleCopperBlockEntity;tryLoadLootTable(Lnet/minecraft/world/level/storage/ValueInput;)Z"))
    private boolean addFilterToLoad1(AbstractSimpleCopperBlockEntity instance, ValueInput input, Operation<Boolean> original) {
        input.read(Filter.FILTER_PATH, CompoundTag.CODEC).ifPresent(tag -> filter = Filter.fromCompound(tag));
        return original.call(instance, input);
    }
    //? }

    @Override
    public boolean canPlaceItem(int i, ItemStack itemStack) {
        return !this.getBlockState().getValue(BlockStateProperties.POWERED)
                && (filter == null || filter.test(itemStack));
    }
}
