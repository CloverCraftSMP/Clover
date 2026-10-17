package com.clovercraftsmp.clover;

//? if <=1.21.1 {

/*import com.clovercraftsmp.clover.util.LootTableUtil;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

*///?}

import com.clovercraftsmp.clover.companion.CcbotClient;
import com.clovercraftsmp.clover.companion.LuckPermsSync;
import com.clovercraftsmp.clover.config.CloverConfig;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import com.clovercraftsmp.clover.util.ItemStackUtil;
import com.clovercraftsmp.clover.util.filter.Filter;
import dev.kikugie.fletching_table.fabric.Entrypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entrypoint(Entrypoint.MAIN)
public class Clover implements ModInitializer {
    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CcbotClient ccbot = new CcbotClient();

    @Override
    public void onInitialize() {
        if (!CloverConfig.HANDLER.load()) {
            Clover.LOGGER.error("Config failed to load!");
        }

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER && CloverConfig.HANDLER.instance().ccbotEnabled) {
            if (FabricLoader.getInstance().isModLoaded("luckperms")) {
                ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                    long[] roles = CcbotClient.ROLES.remove(handler.player.getUUID());
                    if (roles != null) LuckPermsSync.apply(handler.player.getUUID(), roles);
                });
            }

            ccbot.start();
            ServerLifecycleEvents.SERVER_STOPPING.register((server) -> ccbot.shutdown());
        }

        LootTableEvents.MODIFY.register((resourceKey, builder, lootTableSource, provider) -> {
            ItemStackUtil.onEnchantmentRegistryReady(provider);
            //? if <=1.21.1
            //LootTableUtil.fixLootTables(resourceKey, builder);
        });

        PayloadTypeRegistry.clientboundPlay().register(ClientboundSetAfkPacket.TYPE, ClientboundSetAfkPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ClientboundRemoveNoSleepPacket.TYPE, ClientboundRemoveNoSleepPacket.CODEC);

        Filter.registerFilters();
    }

    /**
     * Adapts to the {@link Identifier} changes introduced in 1.21.
     */
    public static Identifier id(String namespace, String path) {
        //? if <1.21 {
        /*return new Identifier(namespace, path);
        *///?} else
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    public static Identifier id(String path) {
        return id(MOD_ID, path);
    }
}