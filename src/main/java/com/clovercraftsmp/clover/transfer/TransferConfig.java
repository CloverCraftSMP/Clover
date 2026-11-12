package com.clovercraftsmp.clover.transfer;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.config.CloverConfig;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class TransferConfig {
    private static final int MIN_KEY_LENGTH = 32;

    private record Settings(SecretKey key, boolean rejectDirectConnection, TransferState.Node self) {}

    private static volatile Settings settings;

    private TransferConfig() {}

    /// Loads the transfer system config. False means the config is fatal.
    public static boolean load(CloverConfig config) {
        settings = null;

        String rawKey = config.transferTokenKey == null ? "" : config.transferTokenKey;
        if (rawKey.length() < MIN_KEY_LENGTH) {
            if (!config.requireTransfer) {
                Clover.LOGGER.warn("Transfer system disabled: transferTokenKey is shorter than {} characters.",
                        MIN_KEY_LENGTH);
                return true;
            } else {
                Clover.LOGGER.error("Transfer system disabled on a server which requires transfers: transferTokenKey is shorter "
                + "than {} characters. Shutting down...", MIN_KEY_LENGTH);
                return false;
            }
        }

        if (config.transferNodeId == null || config.transferNodeId.isBlank()) {
            Clover.LOGGER.error("Transfer system disabled: transferNodeId is empty.");
            return !config.requireTransfer;
        }

        if (config.transferAddress == null || config.transferAddress.isBlank()) {
            Clover.LOGGER.error("Transfer system disabled: transferAddress is empty.");
            return !config.requireTransfer;
        }

        if (config.transferPort < 1 || config.transferPort > 65535) {
            Clover.LOGGER.error("Transfer system disabled: transferPort {} is not a valid port.", config.transferPort);
            return !config.requireTransfer;
        }

        settings = new Settings(
                Keys.hmacShaKeyFor(rawKey.getBytes(StandardCharsets.UTF_8)),
                config.requireTransfer,
                new TransferState.Node(config.transferNodeId, config.transferAddress, config.transferPort)
        );

        Clover.LOGGER.info("Transfer system enabled (node {}, {} direct joins, reachable at {}:{})",
                config.transferNodeId, config.requireTransfer ? "rejecting" : "accepting",
                config.transferAddress, config.transferPort);

        return true;
    }

    public static boolean isEnabled() {
        return settings != null;
    }

    public static boolean rejectsDirect() {
        return require().rejectDirectConnection();
    }

    public static TransferState.Node self() {
        return require().self();
    }

    public static SecretKey key() {
        return require().key();
    }

    private static Settings require() {
        Settings s = settings;
        if (s == null) throw new IllegalStateException("Transfer system is disabled");
        return s;
    }
}
