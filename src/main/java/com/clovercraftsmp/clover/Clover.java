package com.clovercraftsmp.clover;

//? if <=1.21.1 {

/*import com.clovercraftsmp.clover.util.LootTableUtil;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;

*///?}

import com.clovercraftsmp.clover.companion.CcbotClient;
import com.clovercraftsmp.clover.config.CloverConfig;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import com.clovercraftsmp.clover.transfer.FrameStore;
import com.clovercraftsmp.clover.transfer.PendingTransferJoins;
import com.clovercraftsmp.clover.transfer.TransferConfig;
import com.clovercraftsmp.clover.util.DataBaseUtil;
import com.clovercraftsmp.clover.util.ItemStackUtil;
import com.clovercraftsmp.clover.util.filter.Filter;
import dev.kikugie.fletching_table.fabric.Entrypoint;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Entrypoint(Entrypoint.MAIN)
public class Clover implements ModInitializer {
    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final CcbotClient ccbot = new CcbotClient();

    private static final int DB_FLUSH_INTERVAL_TICKS = 20;
    private static final long DB_STARTUP_TIMEOUT_SECONDS = 30;
    private static final long DB_SHUTDOWN_TIMEOUT_SECONDS = 10;
    private static int ticksSinceFlush = 0;

    @Override
    public void onInitialize() {
        if (!CloverConfig.HANDLER.load()) {
            Clover.LOGGER.error("Config failed to load!");
        }

        boolean isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

        if (isServer) {
            FrameStore.init();
            PendingTransferJoins.register();

            ServerLifecycleEvents.SERVER_STARTING.register(server -> {
                if (!TransferConfig.load(CloverConfig.HANDLER.instance())) {
                    throw new IllegalStateException("Transfer system failed to start when required!");
                }

                DataBaseUtil.openAsync()
                        .orTimeout(DB_STARTUP_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .join();
            });

            ServerTickEvents.END_SERVER_TICK.register(server -> {
                if (++ticksSinceFlush >= DB_FLUSH_INTERVAL_TICKS) {
                    ticksSinceFlush = 0;
                    DataBaseUtil.flushAsync();
                }
            });

            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                try {
                    DataBaseUtil.closeAsync().get(DB_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    LOGGER.warn("Database did not close within {} seconds; continuing shutdown.", DB_SHUTDOWN_TIMEOUT_SECONDS);
                } catch (ExecutionException ignored) {
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        if (isServer && CloverConfig.HANDLER.instance().ccbotEnabled) {
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