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

    @SerialEntry(comment = "Require ccbot approval to join. Players are refused while ccbot is unreachable.")
    public boolean ccbotEnabled = true;

    @SerialEntry(comment = "The address that ccbot is running on.")
    public String ccbotAddress = "127.0.0.1";

    @SerialEntry(comment = "The port that ccbot is running on.")
    public int ccbotPort = 25566;

    @SerialEntry(comment = "The passphrase set in ccbot config.")
    public String ccbotPassphrase = "";

    @SerialEntry(comment = "If set to true, players must have the 'clover.maintenance' permission to join.")
    public boolean maintenanceMode = true;
}
