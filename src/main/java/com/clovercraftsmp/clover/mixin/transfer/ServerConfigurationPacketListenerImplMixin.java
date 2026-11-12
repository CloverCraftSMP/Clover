package com.clovercraftsmp.clover.mixin.transfer;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.duck.ConnectionTransferDuck;
import com.clovercraftsmp.clover.duck.TransferDuck;
import com.clovercraftsmp.clover.event.transfer.TransferEvents;
import com.clovercraftsmp.clover.task.CheckTransferCookieTask;
import com.clovercraftsmp.clover.task.VerifyTransferTask;
import com.clovercraftsmp.clover.transfer.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.network.protocol.cookie.ClientboundCookieRequestPacket;
import net.minecraft.network.protocol.cookie.ServerboundCookieResponsePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
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
import java.util.UUID;

import static com.clovercraftsmp.clover.transfer.TransferUtil.COOKIE_PREFIX;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationPacketListenerImplMixin extends ServerCommonPacketListenerImpl implements TransferDuck {
    public ServerConfigurationPacketListenerImplMixin(MinecraftServer server, Connection connection, CommonListenerCookie cookie) {
        super(server, connection, cookie);
    }

    @Shadow @Nullable private ConfigurationTask currentTask;
    @Shadow @Final private Queue<ConfigurationTask> configurationTasks;
    @Shadow @Final private GameProfile gameProfile;
    @Shadow @Final private static Logger LOGGER;
    @Shadow protected abstract void finishCurrentTask(ConfigurationTask.Type type);

    @Unique private static final String ROOT_PATH = COOKIE_PREFIX + "root";

    @Unique private final HashMap<Integer, TransferState.SectionState> receivedSections = new HashMap<>();
    @Unique @Nullable private TransferState.Header header;
    @Unique @Nullable private TransferRouter.Incoming incoming;

    @Unique private int clover$cookieTicks = 0;
    @Unique @Nullable private ConfigurationTask clover$timedTask;
    @Unique private boolean clover$timedOut = false;

    @Unique
    private void clover$processCookie(String path, byte[] payload) {
        if (this.currentTask == null || !this.currentTask.type().equals(CheckTransferCookieTask.TYPE)) return;

        if (payload == null) {
            endCookieCheckEarly("Null payload found on cookie!");
            return;
        }

        String token = new String(payload, StandardCharsets.UTF_8);

        if (path.equals(ROOT_PATH)) {
            if (header != null) {
                endCookieCheckEarly("Root cookie received twice!");
                return;
            }

            Optional<TransferState.Header> optionalHeader = TransferState.Header.fromToken(token);
            if (optionalHeader.isEmpty()) {
                endCookieCheckEarly("Invalid root transfer cookie!");
                return;
            }
            header = optionalHeader.get();

            if (!header.uuid().equals(clover$profileId())) {
                endCookieCheckEarly("Root cookie did not match profile!");
                return;
            }

            if (!header.recipient().id().equals(TransferConfig.self().id())) {
                endCookieCheckEarly("Root cookie was addressed to node " + header.recipient().id() + "!");
                return;
            }

            for (int i = 0; i < header.sectionCount(); i++) {
                connection.send(new ClientboundCookieRequestPacket(Clover.id(COOKIE_PREFIX + i)));
            }

            if (header.sectionCount() > 0) return;
        } else if (header == null) {
            endCookieCheckEarly("Section cookie received before root cookie!");
            return;
        } else {
            int receivedCookieIndex;
            try {
                receivedCookieIndex = Integer.parseInt(path.substring(COOKIE_PREFIX.length()));
            } catch (NumberFormatException e) {
                endCookieCheckEarly("Invalid index in section cookie path!");
                return;
            }

            Optional<TransferState.SectionState> optionalSection = TransferState.SectionState.fromToken(token);
            if (optionalSection.isEmpty()) {
                endCookieCheckEarly("Invalid section transfer cookie!");
                return;
            }
            TransferState.SectionState sectionState = optionalSection.get();

            if (!sectionState.belongsTo(header)) {
                endCookieCheckEarly("Section cookie did not belong to the root cookie!");
                return;
            }

            int sectionID = sectionState.sectionID();
            if (sectionID != receivedCookieIndex) {
                endCookieCheckEarly("Section cookie index did not match name!");
                return;
            }

            if (receivedSections.containsKey(sectionID)) {
                endCookieCheckEarly("Duplicate cookie section received!");
                return;
            }

            receivedSections.put(sectionID, sectionState);
            if (receivedSections.size() != header.sectionCount()) return;
        }

        Optional<TransferState.Frame> optionalFrame = header.withSections(receivedSections);
        if (optionalFrame.isEmpty()) {
            endCookieCheckEarly("Failed to build frame from cookies!");
            return;
        }

        incoming = new TransferRouter.Incoming(header.kind(), optionalFrame.get());
        this.finishCurrentTask(CheckTransferCookieTask.TYPE);
    }

    @Unique
    private void endCookieCheckEarly(String reason) {
        LOGGER.warn("Invalid cookie state: {}", reason);
        this.finishCurrentTask(CheckTransferCookieTask.TYPE);
    }

    @Override
    public void clover$route() {
        TransferRouter.routeAsync(clover$profileId(), incoming).handleAsync((decision, error) -> {
            try {
                if (clover$timedOut || !this.connection.isConnected()) return null;
                if (error != null) {
                    this.disconnect(Component.literal("Failed to load transfer state, please try again."));
                    return null;
                }
                switch (decision) {
                    case TransferRouter.Accept accept -> clover$accept(accept.arrival());
                    case TransferRouter.Send send ->
                            TransferUtil.buildPackets(send.kind(), send.frame()).forEach(this.connection::send);
                    case TransferRouter.Reject reject -> this.disconnect(Component.literal(reject.reason()));
                }
            } catch (Exception e) {
                this.disconnect(Component.literal("An error occurred while processing transfer state, please try again."));
            }
            return null;
        }, this.server);
    }

    @Unique
    private void clover$accept(@Nullable TransferEvents.Arrival arrival) {
        UUID uuid = clover$profileId();
        ((ConnectionTransferDuck) this.connection).clover$setTransferVerified();
        TransferRouter.setCurrent(uuid, arrival == null ? null : arrival.current());
        if (arrival != null) {
            TransferEvents.ARRIVED_CONFIGURATION.invoker().onArrived((ServerConfigurationPacketListener) this, arrival);
            PendingTransferJoins.put(uuid, arrival);
        }
        this.finishCurrentTask(VerifyTransferTask.TYPE);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void clover$cookieTimeout(CallbackInfo ci) {
        if (clover$timedOut) return;

        ConfigurationTask task = this.currentTask;
        if (task == null || !(task.type().equals(CheckTransferCookieTask.TYPE) || task.type().equals(VerifyTransferTask.TYPE))) {
            clover$timedTask = null;
            return;
        }

        if (task != clover$timedTask) {
            clover$timedTask = task;
            clover$cookieTicks = 0;
        }

        if (++clover$cookieTicks > 20 * 10 && this.connection.isConnected()) {
            clover$timedOut = true;
            this.disconnect(Component.literal("Timed out during transfer check."));
        }
    }

    @Inject(method = "startConfiguration", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;addOptionalTasks()V"
    ))
    private void addCustomTasks(CallbackInfo ci) {
        if (((ConnectionTransferDuck) this.connection).clover$isTransferVerified()) return;
        PendingTransferJoins.clear(clover$profileId());

        if (!TransferConfig.isEnabled()) return;

        if (((TransferAccessor) this).isTransfer()) {
            this.configurationTasks.add(new CheckTransferCookieTask());
        }
        this.configurationTasks.add(new VerifyTransferTask(this));
    }

    @Override
    public boolean clover$handleTransferCookie(ServerboundCookieResponsePacket cookie) {
        var loc = cookie.key();
        String path = loc.getPath();
        if (!loc.getNamespace().equals(Clover.MOD_ID) || !path.startsWith(COOKIE_PREFIX)) return false;

        byte[] payload = cookie.payload();
        this.server.execute(() -> {
            if (this.connection.isConnected()) clover$processCookie(path, payload);
        });

        return true;
    }

    @Unique
    private UUID clover$profileId() {
        //? if >=26.1
        return this.gameProfile.id();
        //? if <=1.21.1
        //return this.gameProfile.getId();
    }
}
