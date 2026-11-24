package com.clovercraftsmp.clover.mixin.feature.bonemealable;

import com.clovercraftsmp.clover.tags.CloverTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
        //? if >= 26.1
        net.minecraft.world.level.block.LilyPadBlock.class,
        //? if <= 1.21.1
        //net.minecraft.world.level.block.WaterlilyBlock.class,
        net.minecraft.world.level.block.FlowerBlock.class,
})

public class BonemealDuplicationMixin implements BonemealableBlock {
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.is(CloverTags.DUPLICABLE_FLOWERS);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Block.popResource(level, pos, new ItemStack((ItemLike) this));
    }
}
