package com.clovercraftsmp.clover;

import com.clovercraftsmp.clover.config.CloverConfig;
import com.clovercraftsmp.clover.event.feature.CauldronRecipeTickHandler;
import com.clovercraftsmp.clover.networking.ClientboundRemoveNoSleepPacket;
import com.clovercraftsmp.clover.networking.ClientboundSetAfkPacket;
import com.clovercraftsmp.clover.util.ItemStackUtil;
import com.clovercraftsmp.clover.util.LootTableUtil;
import com.clovercraftsmp.clover.util.filter.Filter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clover implements ModInitializer {
    public static final GameRules.Key<GameRules.BooleanValue> PUNISH_INSOMNIACS =
            GameRuleRegistry.register(
                    "punishInsomniacs",
                    GameRules.Category.MOBS,
                    GameRuleFactory.createBooleanRule(false)
            );

    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        if (!CloverConfig.HANDLER.load()) {
            Clover.LOGGER.error("Config failed to load!");
        }

        LootTableEvents.MODIFY.register((resourceKey, builder, lootTableSource, provider) -> {
            ItemStackUtil.onEnchantmentRegistryReady(provider);
            LootTableUtil.fixLootTables(resourceKey, builder);
        });

        CauldronRecipeTickHandler.register();

        PayloadTypeRegistry.playS2C().register(ClientboundSetAfkPacket.TYPE, ClientboundSetAfkPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ClientboundRemoveNoSleepPacket.TYPE, ClientboundRemoveNoSleepPacket.CODEC);

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