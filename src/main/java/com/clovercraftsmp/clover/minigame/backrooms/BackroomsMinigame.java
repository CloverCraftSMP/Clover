package com.clovercraftsmp.clover.minigame.backrooms;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.minigame.Minigame;
import com.clovercraftsmp.clover.minigame.backrooms.entity.BackroomsEndermanEntity;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import java.util.Random;

public class BackroomsMinigame implements Minigame {
    public static EntityType<BackroomsEndermanEntity> BACKROOMS_ENDERMAN;

    @Override
    public String getId() {
        return "backrooms";
    }

    @Override
    public void registerWorldGen() {
        Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Clover.id("backrooms_generator"), BackroomsChunkGenerator.CODEC);

        FabricLoader.getInstance().getModContainer("clover").ifPresent(container -> {
            ResourceManagerHelper.registerBuiltinResourcePack(
                    Clover.id("backrooms_dim"),
                    container,
                    ResourcePackActivationType.DEFAULT_ENABLED
            );
        });
    }

    @Override
    public void registerEntities() {
        BACKROOMS_ENDERMAN = Registry.register(
                BuiltInRegistries.ENTITY_TYPE,
                Clover.id( "backrooms_enderman"),
                EntityType.Builder.of(BackroomsEndermanEntity::new, MobCategory.MONSTER)
                        .sized(0.6F, 2.9F)
                        .eyeHeight(2.55F)
                        .clientTrackingRange(8)
                        .build("backrooms_enderman")
        );

        FabricDefaultAttributeRegistry.register(BACKROOMS_ENDERMAN, EnderMan.createAttributes());
    }

    @Override
    public void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            ServerPlayer player = handler.player;

            ResourceKey<Level> backroomsKey = ResourceKey.create(Registries.DIMENSION, Clover.id( "backrooms"));
            ServerLevel backroomsLevel = server.getLevel(backroomsKey);

            if (backroomsLevel != null) {
                Random random = new Random();
                int spawnX = (random.nextInt(20000) - 10000);
                int spawnZ = (random.nextInt(20000) - 10000);
                int spawnY = 0;

                player.teleportTo(backroomsLevel, spawnX, spawnY, spawnZ, 0, 0);

                BackroomsEndermanEntity stalker = BACKROOMS_ENDERMAN.create(backroomsLevel);
                if (stalker != null) {
                    stalker.moveTo(spawnX + 30, spawnY, spawnZ + 30, 0, 0);
                    stalker.setTargetPlayerId(player.getUUID());
                    backroomsLevel.addFreshEntity(stalker);
                }
            }
        }));
    }
}
