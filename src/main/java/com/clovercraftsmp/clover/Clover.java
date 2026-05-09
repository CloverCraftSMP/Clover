package com.clovercraftsmp.clover;

import com.clovercraftsmp.clover.util.ItemStackUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clover implements ModInitializer {
    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.3.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    @Override
    public void onInitialize() {
        LootTableEvents.MODIFY.register(ItemStackUtil::mendingRegistry);
    }

    /**
     * Adapts to the {@link ResourceLocation} changes introduced in 1.21.
     */
    public static ResourceLocation id(String namespace, String path) {
        //? if <1.21 {
        /*return new ResourceLocation(namespace, path);
        *///?} else
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }

    public static ResourceLocation id(String path) {
        return id(MOD_ID, path);
    }
}