package com.clovercraftsmp.clover.mixin.companion;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.companion.CcbotClient;
import com.clovercraftsmp.clover.compat.LuckPermsSync;
import com.clovercraftsmp.clover.companion.PlayerState;
import com.clovercraftsmp.clover.companion.protocol.CcbotProtocol;
import com.clovercraftsmp.clover.config.CloverConfig;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.1
import net.minecraft.server.players.NameAndId;

import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(ServerLoginPacketListenerImpl.class)
public class ServerLoginPacketListenerImplMixin {
    @Shadow
    @Final
    private MinecraftServer server;
    @Unique private CompletableFuture<CcbotProtocol.PlayerStateResponse> clover$ccbotState;

    @Inject(method = "startClientVerification", at = @At("HEAD"))
    private void startCcbotQuery(GameProfile profile, CallbackInfo ci) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) return;
        if (!CloverConfig.HANDLER.instance().ccbotEnabled) return;

        clover$ccbotState = Clover.ccbot.getState(
                //? if <=1.21.1
                //profile.getId()
                //? if >=26.1
                profile.id()
        ).exceptionally(e -> CcbotProtocol.PlayerStateResponse.FAILED);
    }

    @Inject(method = "verifyLoginAndFinishConnectionSetup", at = @At("HEAD"), cancellable = true)
    private void waitForCcbot(GameProfile gameProfile, CallbackInfo ci) {
        if (clover$ccbotState != null && !clover$ccbotState.isDone()) ci.cancel();
    }

    @WrapOperation(
            method = "verifyLoginAndFinishConnectionSetup",
            at = @At(
                    value = "INVOKE",
                    //? if <=1.21.1
                    //target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/network/chat/Component;"
                    //? if >=26.1
                    target = "Lnet/minecraft/server/players/PlayerList;canPlayerLogin(Ljava/net/SocketAddress;Lnet/minecraft/server/players/NameAndId;)Lnet/minecraft/network/chat/Component;"
            )
    )
    private Component applyCcbotCheck(
            PlayerList instance,
            SocketAddress address,
            //? if <=1.21.1
            //GameProfile profile,
            //? if >=26.1
            NameAndId profile,
            Operation<Component> original
    ) {
        UUID id =
                //? if >=26.1
                profile.id();
                //? if <=1.21.1
                //profile.getId();

        CcbotClient.APPROVED.remove(id);
        if (clover$ccbotState == null) return clover$finishLogin(instance, address, profile, id, original);

        CcbotProtocol.PlayerStateResponse response = clover$ccbotState.getNow(CcbotProtocol.PlayerStateResponse.FAILED);

        if (instance.isUsingWhitelist() && instance.isWhiteListed(profile)) {
            if (response.state() == PlayerState.APPROVED) clover$applyRoles(id, response.roleIds());
            return clover$finishLogin(instance, address, profile, id, original);
        }

        return switch (response.state()) {
            case BANNED -> Component.literal("Your account is banned through ccbot! If you believe this is an error, contact the mod team.");
            case NEW -> Component.literal("Please join auth.clovercraftsmp.com to link your account!");
            case PENDING -> Component.literal("Your account is pending admin approval. Please try again later!");
            case FAILED -> Component.literal("Ccbot was unable to verify your status at this time. Please try again later!");
            case APPROVED -> {
                clover$applyRoles(id, response.roleIds());
                CcbotClient.APPROVED.add(id);
                yield clover$finishLogin(instance, address, profile, id, original);
            }
        };
    }

    @Unique
    private static void clover$applyRoles(UUID id, long[] roleIds) {
        if (FabricLoader.getInstance().isModLoaded("luckperms")) LuckPermsSync.apply(id, roleIds);
    }

    @Unique
    private Component clover$finishLogin(
            PlayerList instance,
            SocketAddress address,
            //? if <=1.21.1
            //GameProfile profile,
            //? if >=26.1
            NameAndId profile,
            UUID id,
            Operation<Component> original
    ) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER
        && CloverConfig.HANDLER.instance().maintenanceMode
        && !clover$canBypassMaintenance(id)) {
            return Component.literal("The server is currently under maintenance. Please try again later!");
        }
        return original.call(instance, address, profile);
    }

    @Unique
    private static boolean clover$canBypassMaintenance(UUID id) {
        if (FabricLoader.getInstance().isModLoaded("luckperms")) {
            return LuckPermsSync.hasPermission(id, "clover.maintenance");
        }
        return true;
    }
}
