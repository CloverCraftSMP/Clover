package com.clovercraftsmp.clover.mixin.transfer;

import com.clovercraftsmp.clover.duck.TransferDuck;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("ConstantValue")
@Mixin(ServerCommonPacketListenerImpl.class)
public class ServerCommonPacketListenerImplMixin {
    @WrapMethod(method = "handleCookieResponse")
    private void addTransferCookieCheck(ServerboundCookieResponsePacket cookie, Operation<Void> original) {
        if (cookie.payload() == null || !(this instanceof TransferDuck duck) || !duck.clover$handleTransferCookie(cookie)) {
            original.call(cookie);
        }
    }
}
