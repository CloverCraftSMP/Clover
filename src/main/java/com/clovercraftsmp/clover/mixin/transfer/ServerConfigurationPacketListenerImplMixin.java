package com.clovercraftsmp.clover.mixin.transfer;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.duck.TransferDuck;
import com.clovercraftsmp.clover.event.transfer.TransferEvents;
import com.clovercraftsmp.clover.task.CheckTransferCookieTask;
import com.clovercraftsmp.clover.task.VerifyTransferTask;
import com.clovercraftsmp.clover.util.DataBaseUtil;
import com.clovercraftsmp.clover.util.DataBaseUtil.BaseState;
import com.clovercraftsmp.clover.util.DataBaseUtil.PlayerState;
import com.clovercraftsmp.clover.util.DataBaseUtil.SectionState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundStoreCookiePacket;
import net.minecraft.network.protocol.common.ClientboundTransferPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ConfigurationTask.Type;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
    @Shadow @Final private static Logger LOGGER;
    @Shadow protected abstract void finishCurrentTask(Type type);

    @Unique private final HashMap<Integer, SectionState> receivedSections = new HashMap<>();
    @Unique private int sectionCount = -1;
    @Unique private BaseState baseState;
    @Unique private boolean transferVerified = false;

    @Unique private PlayerState localState;
    @Unique private PlayerState cookieState;

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

        ResourceLocation loc = cookie.key();
        String path = loc.getPath();
        if (!loc.getNamespace().equals(Clover.MOD_ID)) return false;
        if (!path.startsWith("transfer_cookie_")) return false;
        byte[] payload = cookie.payload();
        String token = new String(payload, StandardCharsets.UTF_8);

        if (path.equals("transfer_cookie_root")) {
            if (baseState != null) return endCookieCheckEarly("BaseState received twice!");
            Optional<BaseState> optionalBase = BaseState.fromToken(token);
            if (optionalBase.isEmpty()) return endCookieCheckEarly("Invalid root transfer cookie!");
            baseState = optionalBase.get();
            if (!baseState.uuid().equals(this.gameProfile.getId())) return endCookieCheckEarly("Base state cookie does not match profile!");
            sectionCount = baseState.sectionCount();
            for (int i = 0; i < sectionCount; i++) {
                connection.send(new ClientboundCookieRequestPacket(sectionLocation(i)));
            }
            if (sectionCount > 0) {
                return true;
            }
        } else if (baseState == null) {
            return endCookieCheckEarly("Section cookie received before root cookie!");
        } else if (receivedSections.size() < sectionCount) {
            int receivedCookieIndex;
            try {
                receivedCookieIndex = Integer.parseInt(path.substring(16));
            } catch (NumberFormatException e) {
                return endCookieCheckEarly("Invalid index in section cookie path!");
            }

            Optional<SectionState> optionalSection = SectionState.fromToken(token);
            if (optionalSection.isEmpty()) return endCookieCheckEarly("Invalid section transfer cookie!");
            SectionState sectionState = optionalSection.get();

            if (!baseState.belongs(sectionState)) return endCookieCheckEarly("Section cookie did not belong to the base transfer!");

            int sectionID = sectionState.sectionID();
            if (sectionID != receivedCookieIndex) return endCookieCheckEarly("Section cookie index did not match name!");
            if (receivedSections.containsKey(sectionID)) return endCookieCheckEarly("Duplicate cookie section received!");

            receivedSections.put(sectionID, sectionState);
            if (receivedSections.size() != sectionCount) return true;
        }

        Optional<PlayerState> optionalCookieState = PlayerState.fromBaseAndSections(baseState, receivedSections);
        if (optionalCookieState.isEmpty()) return endCookieCheckEarly("Failed to build PlayerState from Base and Sections!");
        cookieState = optionalCookieState.get();
        transferVerified = true;

        localState = DataBaseUtil.getUserAsync(this.gameProfile.getId()).join().orElse(null);
        boolean isSameSession = cookieState.isSameSessionAs(localState);

        if (DataBaseUtil.isMainServer) {
            DataBaseUtil.deleteUserAsync(this.gameProfile.getId()).join();
            TransferEvents.RECEIVED_CONFIGURATION.invoker().onReceived((ServerConfigurationPacketListener) this, cookieState, localState);
            this.finishCurrentTask(CheckTransferCookieTask.TYPE);
        } else if (isSameSession && localState.complete()) {
            PlayerState outboundState = localState.withMyLocation();
            connection.send(new ClientboundStoreCookiePacket(
                    CheckTransferCookieTask.TRANSFER_COOKIE_ROOT,
                    outboundState.getBase().toToken().getBytes(StandardCharsets.UTF_8)
            ));

            HashMap<Integer, String> sections = outboundState.getSections();
            for (int i = 0; i < sections.size(); i++) {
                connection.send(new ClientboundStoreCookiePacket(
                        sectionLocation(i),
                        sections.get(i).getBytes(StandardCharsets.UTF_8)
                ));
            }

            connection.send(new ClientboundTransferPacket(cookieState.hostname(), cookieState.port()));
        } else {
            DataBaseUtil.updateUserAsync(cookieState).join();
            TransferEvents.RECEIVED_CONFIGURATION.invoker().onReceived((ServerConfigurationPacketListener) this, cookieState, localState);
            this.finishCurrentTask(CheckTransferCookieTask.TYPE);
        }

        return true;
    }

    @Unique
    private static ResourceLocation sectionLocation(int section) {
        return ResourceLocation.parse("clover:transfer_cookie_" + section);
    }

    @Unique
    private boolean endCookieCheckEarly(String reason) {
        LOGGER.warn("Invalid cookie state: {}", reason);
        this.finishCurrentTask(CheckTransferCookieTask.TYPE);
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

    @WrapOperation(method = "handleConfigurationFinished", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;placeNewPlayer(Lnet/minecraft/network/Connection;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/server/network/CommonListenerCookie;)V"))
    private void wrapPlaceNewPlayer(PlayerList instance, Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, Operation<Void> original) {
        original.call(instance, connection, serverPlayer, commonListenerCookie);
        if (cookieState != null) {
            TransferEvents.RECEIVED_PLAY.invoker().onReceived(serverPlayer, cookieState, localState);
        }
    }
}
