package com.clovercraftsmp.clover.task;

import com.clovercraftsmp.clover.transfer.TransferUtil;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

public class CheckTransferCookieTask implements ConfigurationTask {
    public static final Type TYPE = new Type("clover:get_transfer_cookie");

    @Override
    public void start(Consumer<Packet<?>> consumer) {
        consumer.accept(new ClientboundCookieRequestPacket(TransferUtil.ROOT_COOKIE));
    }

    @Override @NotNull
    public Type type() {
        return TYPE;
    }
}
