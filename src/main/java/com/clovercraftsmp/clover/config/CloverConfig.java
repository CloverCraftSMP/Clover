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

    @SerialEntry(comment = "The key used for verifying transfer tokens. If less than 32 characters, the transfer system is disabled.")
    public String transferTokenKey = "";

    @SerialEntry(comment = "Whether the transfer system is required for entry to the server.")
    public boolean requireTransfer = false;

    @SerialEntry(comment = "Unique ID of this node in the transfer network. Required. Do not change this once players have been transferred.")
    public String transferNodeId = "";

    @SerialEntry(comment = "The address other servers send players to when transferring them back here.")
    public String transferAddress = "localhost";

    @SerialEntry(comment = "The port other servers send players to when transferring them back here.")
    public int transferPort = 25565;
}
