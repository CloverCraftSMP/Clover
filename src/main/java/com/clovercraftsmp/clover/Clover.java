package com.clovercraftsmp.clover;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clover implements ModInitializer {
    public static final String MOD_ID = "clover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "0.1.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    @Override
    public void onInitialize() {

        LOGGER.info("Hello Fabric world!");

        //? if !release
        //LOGGER.warn("I'm still a template!");

        //? if fapi: <0.100
        /*LOGGER.info("Fabric API is old on this version");*/
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
}