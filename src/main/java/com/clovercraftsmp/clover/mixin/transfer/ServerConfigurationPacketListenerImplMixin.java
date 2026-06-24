package com.clovercraftsmp.clover.mixin.transfer;

import com.clovercraftsmp.clover.duck.TransferDuck;
import com.clovercraftsmp.clover.task.CheckTransferCookieTask;
import com.clovercraftsmp.clover.task.VerifyTransferTask;
import com.clovercraftsmp.clover.util.DataBaseUtil;
import com.clovercraftsmp.clover.util.DataBaseUtil.PlayerState;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Queue;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl implements TransferDuck {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer minecraftServer, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraftServer, connection, commonListenerCookie);
    }

    @Shadow @Nullable private ConfigurationTask currentTask;
    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;
    @Shadow @Final private GameProfile gameProfile;
    @Shadow protected abstract void finishCurrentTask(Type type);
    @Unique private boolean transferVerified = false;

    @Inject(method = "startConfiguration", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;addOptionalTasks()V",
            shift = At.Shift.AFTER)
    )
    private void addCustomTasks(CallbackInfo ci) {
        if (((TransferAccessor) this).isTransfer()) {
            this.configurationTasks.add(new CheckTransferCookieTask());
        } else if (DataBaseUtil.isMainServer) {
            transferVerified = true;
        }
        this.configurationTasks.add(new VerifyTransferTask(this));
    }

    @Override
    public boolean clover$handleTransferCookie(ServerboundCookieResponsePacket cookie) {
        if (this.currentTask == null || !this.currentTask.type().equals(CheckTransferCookieTask.TYPE)) return false;
        if (!cookie.key().equals(CheckTransferCookieTask.TRANSFER_COOKIE)) return false;

        byte[] payload = cookie.payload();
        Optional<PlayerState> decodedState = PlayerState.fromToken(new String(payload));
        if (decodedState.isEmpty()) {
            this.finishCurrentTask(CheckTransferCookieTask.TYPE);
            return true;
        }

        transferVerified = true;

        PlayerState cookieState = decodedState.get();
        PlayerState localState = DataBaseUtil.getUserAsync(this.gameProfile.getId()).join().orElse(null);
        boolean isSameSession = cookieState.isSameSessionAs(localState);

        if (DataBaseUtil.isMainServer) {
            DataBaseUtil.deleteUserAsync(cookieState.uuid()).join();
            // TODO: Do something with cookieState.transferID() and cookieState.extra() somewhere
            this.finishCurrentTask(CheckTransferCookieTask.TYPE);
        } else if (isSameSession && localState.complete()) {
            String token = localState.withMyLocation().toToken();
            connection.send(new ClientboundStoreCookiePacket(CheckTransferCookieTask.TRANSFER_COOKIE, token.getBytes()));
            connection.send(new ClientboundTransferPacket(cookieState.hostname(), cookieState.port()));
        } else {
            DataBaseUtil.updateUserAsync(cookieState).join();
            // TODO: Do something with cookieState.transferID(), cookieState.extra(), and isSameSession somewhere
            this.finishCurrentTask(CheckTransferCookieTask.TYPE);
        }

        return true;
    }

    @Override
    public void clover$checkVerify() {
        if (transferVerified) {
            this.finishCurrentTask(VerifyTransferTask.TYPE);
        } else {
            this.disconnect(Component.literal("Missing or invalid transfer cookie!"));
        }
    }
}
