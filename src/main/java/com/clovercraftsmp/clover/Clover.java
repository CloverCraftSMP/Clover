package com.clovercraftsmp.clover;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Clover implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("clover");
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