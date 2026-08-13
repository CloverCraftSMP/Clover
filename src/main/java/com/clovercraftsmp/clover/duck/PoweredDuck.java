package com.clovercraftsmp.clover.duck;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface PoweredDuck {
    boolean clover$isPowered();
    boolean clover$canTransferPoweredCheck(Level level, BlockPos pos);
}
