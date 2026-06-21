package com.clovercraftsmp.clover.client;

import com.clovercraftsmp.clover.compat.StatusCompatImpl;
import com.clovercraftsmp.clover.minigame.backrooms.BackroomsMinigame;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;

public class CloverClient implements ClientModInitializer {
    private static final boolean STATUS_PRESENT = FabricLoader.getInstance().isModLoaded("status");

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundSetAfkPacket.TYPE, (payload, context) -> {
            if (STATUS_PRESENT) StatusCompatImpl.updateAfk(payload);
        });

        ClientPlayNetworking.registerGlobalReceiver(ClientboundRemoveNoSleepPacket.TYPE, (payload, context) -> {
            if (STATUS_PRESENT) StatusCompatImpl.removeNoSleep();
        });

        if (BackroomsMinigame.BACKROOMS_ENDERMAN != null) {
            EntityRendererRegistry.register(BackroomsMinigame.BACKROOMS_ENDERMAN, EndermanRenderer::new);
        }
    }
}
