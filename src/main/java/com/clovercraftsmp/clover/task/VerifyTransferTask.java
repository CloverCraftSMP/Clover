package com.clovercraftsmp.clover.task;

import com.clovercraftsmp.clover.duck.TransferDuck;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class VerifyTransferTask implements ConfigurationTask {
    public static final Type TYPE = new Type("clover:verify_transfer_success");
    private final TransferDuck duck;

    public VerifyTransferTask(TransferDuck duck) {
        this.duck = duck;
    }

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        this.duck.clover$checkVerify();
    }

    @Override @NotNull
    public Type type() {
        return TYPE;
    }
}
