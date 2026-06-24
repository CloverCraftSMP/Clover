package com.clovercraftsmp.clover.duck;

import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;

public interface TransferDuck {
    boolean clover$handleTransferCookie(ServerboundCookieResponsePacket cookie);
    void clover$checkVerify();
}
