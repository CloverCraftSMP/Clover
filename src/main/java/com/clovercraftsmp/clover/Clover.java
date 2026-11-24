package com.clovercraftsmp.clover;

//? if <=1.21.1 {
/*import com.clovercraftsmp.clover.util.LootTableUtil;
*///?}

//? if >= 26.1 {
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
//? }

import com.clovercraftsmp.clover.companion.CcbotClient;
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

        boolean isServer = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;

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

        // I can't depend on backport for datagen because it breaks Carpet,
        // which I need during dev tests to ensure player drops work.
        // This is the dumbest workaround in the codebase by far...
        //? if >= 26.1 {
        if (System.getProperty("fabric-api.datagen") != null) {
            for (String name : List.of(
                    "poplar_log", "stripped_poplar_log", "poplar_wood", "stripped_poplar_wood",
                    "poplar_planks", "poplar_stairs", "poplar_slab", "poplar_fence", "poplar_fence_gate",
                    "poplar_door", "poplar_trapdoor", "poplar_button", "poplar_pressure_plate",
                    "poplar_sign", "poplar_hanging_sign", "poplar_shelf")) {
                ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace(name));
                Registry.register(BuiltInRegistries.ITEM, key, new Item(new Item.Properties().setId(key)));
            }
        }
        //? }
        // Please forgive me, Syfe... I'm just too tired to do this "correctly"...
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