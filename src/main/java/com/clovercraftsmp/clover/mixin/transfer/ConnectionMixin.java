package com.clovercraftsmp.clover.mixin.transfer;

import com.clovercraftsmp.clover.duck.ConnectionTransferDuck;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Connection.class)
public class ConnectionMixin implements ConnectionTransferDuck {
    @Unique private boolean clover$transferVerified;

    @Override
    public boolean clover$isTransferVerified() { return clover$transferVerified; }

    @Override
    public void clover$setTransferVerified() { clover$transferVerified = true; }
}
