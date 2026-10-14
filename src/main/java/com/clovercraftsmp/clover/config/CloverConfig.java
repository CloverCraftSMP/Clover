package com.clovercraftsmp.clover.config;

import com.clovercraftsmp.clover.Clover;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;

public class CloverConfig {
    public static ConfigClassHandler<CloverConfig> HANDLER = ConfigClassHandler.createBuilder(CloverConfig.class)
            .id(Clover.id("clover_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("clover.json5"))
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public String minigameType = "none";
}
