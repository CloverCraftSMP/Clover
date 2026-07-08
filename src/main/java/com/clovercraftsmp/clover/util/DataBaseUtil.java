package com.clovercraftsmp.clover.util;

import com.mojang.logging.LogUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.*;

public class DataBaseUtil {
    public record BaseState(UUID uuid, UUID session, String hostname, int port, boolean complete, String transferID, int sectionCount) {
        public static Optional<BaseState> fromToken(String token) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireSubject("transfer")
                        .requireIssuer("CloverCraft")
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                return Optional.of(new BaseState(
                        UUID.fromString(claims.get("uuid", String.class)),
                        UUID.fromString(claims.get("session", String.class)),
                        claims.get("hostname", String.class),
                        claims.get("port", Integer.class),
                        claims.get("complete", Boolean.class),
                        claims.get("transferID", String.class),
                        claims.get("sectionCount", Integer.class)
                ));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        public String toToken() {
            return Jwts.builder()
                    .subject("transfer")
                    .issuer("CloverCraft")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + PlayerState.EXPIRATION_TIME))
                    .claim("uuid", this.uuid.toString())
                    .claim("session", this.session.toString())
                    .claim("hostname", this.hostname)
                    .claim("port", this.port)
                    .claim("complete", this.complete)
                    .claim("transferID", this.transferID)
                    .claim("sectionCount", this.sectionCount)
                    .signWith(key)
                    .compact();
        }

        public boolean belongs(SectionState sectionState) {
            return sectionState.uuid().equals(this.uuid()) && sectionState.session().equals(this.session());
        }
    }

    public record SectionState(UUID uuid, UUID session, int sectionID, byte[] sectionData) {
        public static SectionState fromFullState(PlayerState state, int sectionID, byte[] data) {
            return new SectionState(state.uuid(), state.session(), sectionID, data);
        }

        public static Optional<SectionState> fromToken(String token) {
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(key)
                        .requireSubject("transfer")
                        .requireIssuer("CloverCraft")
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String encodedData = claims.get("sectionData", String.class);
                byte[] data = Base64.getUrlDecoder().decode(encodedData);

                return Optional.of(new SectionState(
                        UUID.fromString(claims.get("uuid", String.class)),
                        UUID.fromString(claims.get("session", String.class)),
                        claims.get("sectionID", Integer.class),
                        data
                ));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        public String toToken() {
            String encodedData = Base64.getUrlEncoder().encodeToString(this.sectionData);

            return Jwts.builder()
                    .subject("transfer")
                    .issuer("CloverCraft")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + PlayerState.EXPIRATION_TIME))
                    .claim("uuid", this.uuid.toString())
                    .claim("session", this.session.toString())
                    .claim("sectionID", this.sectionID)
                    .claim("sectionData", encodedData)
                    .signWith(key)
                    .compact();
        }
    }

    public record PlayerState(UUID uuid, UUID session, String hostname, int port, boolean complete, String transferID, byte[] extra) {
        private static final long EXPIRATION_TIME = 30_000;
        private static final int DATA_SPLIT_SIZE = 3000;

        public static Optional<PlayerState> fromBaseAndSections(BaseState base, HashMap<Integer, SectionState> sections) {
            if (sections.size() != base.sectionCount()) return Optional.empty();

            for (int i = 0; i < sections.size(); i++) {
                if (!sections.containsKey(i)) return Optional.empty();
            }

            int totalLength = 0;
            for (SectionState sectionState : sections.values()) totalLength += sectionState.sectionData().length;

            byte[] combinedData = new byte[totalLength];
            int offset = 0;
            for (int i = 0; i < sections.size(); i++) {
                byte[] data = sections.get(i).sectionData();
                System.arraycopy(data, 0, combinedData, offset, data.length);
                offset += data.length;
            }

            return Optional.of(new PlayerState(base.uuid(), base.session(), base.hostname(), base.port(), base.complete(), base.transferID(), combinedData));
        }

        public boolean isSameSessionAs(PlayerState other) {
            return other != null && this.uuid.equals(other.uuid) && this.session.equals(other.session);
        }

        public PlayerState withMyLocation() {
            return new PlayerState(this.uuid, this.session, serverAddress, serverPort, this.complete, this.transferID, this.extra);
        }

        public BaseState getBase() {
            int sections = Math.ceilDiv(this.extra.length, PlayerState.DATA_SPLIT_SIZE);
            return new BaseState(this.uuid, this.session, this.hostname, this.port, this.complete, this.transferID, sections);
        }

        public HashMap<Integer, String> getSections() {
            int sectionCount = Math.ceilDiv(this.extra.length, DATA_SPLIT_SIZE);

            HashMap<Integer, String> out = new HashMap<>();
            for (int i = 0; i < sectionCount; i++) {
                int start = i * DATA_SPLIT_SIZE;
                int end = Math.min(start + DATA_SPLIT_SIZE, this.extra.length);
                byte[] chunk = Arrays.copyOfRange(this.extra, start, end);
                out.put(i, SectionState.fromFullState(this, i, chunk).toToken());
            }

            return out;
        }
    }

    private static volatile Connection connection;
    private static final String URL = "jdbc:sqlite:clover.db";
    private static volatile SecretKey key;
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ConcurrentHashMap<UUID, PlayerState> CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Optional<PlayerState>> PENDING_WRITES = new ConcurrentHashMap<>();

    public static final boolean isMainServer = Boolean.parseBoolean(System.getProperty("isMainServer", "true"));
    public static final String serverAddress = System.getProperty("serverAddress", "localhost");
    public static final int serverPort = Integer.parseInt(System.getProperty("serverPort", "25565"));

    private static final ExecutorService DB_EXEC = Executors.newSingleThreadExecutor(
            r -> {
                Thread t = new Thread(r, "clover-db");
                t.setDaemon(true);
                return t;
            }
    );

    public static CompletableFuture<Optional<PlayerState>> getUserAsync(UUID user) {
        PlayerState cached = CACHE.get(user);
        if (cached != null) return CompletableFuture.completedFuture(Optional.of(cached));

        Optional<PlayerState> pending = PENDING_WRITES.get(user);
        if (pending != null) return CompletableFuture.completedFuture(pending);

        return CompletableFuture.supplyAsync(() -> getUser(user), DB_EXEC).exceptionally(t -> {
            LOGGER.error("getUserAsync failed for {}: {}", user, t.getMessage(), t);
            return Optional.empty();
        });
    }

    public static CompletableFuture<Void> openAsync() {
        return CompletableFuture.runAsync(DataBaseUtil::open, DB_EXEC)
                .exceptionally(t -> {
                    LOGGER.error("openAsync failed: {}", t.getMessage(), t);
                    return null;
                });
    }

    public static CompletableFuture<Void> closeAsync() {
        return CompletableFuture.runAsync(DataBaseUtil::close, DB_EXEC)
                .whenComplete((r, t) -> {
                    if (t != null) {
                        LOGGER.error("closeAsync failed: {}", t.getMessage(), t);
                    }

                    DB_EXEC.shutdown();

                    try {
                        if (!DB_EXEC.awaitTermination(5, TimeUnit.SECONDS)) {
                            LOGGER.warn("DB Executor did not terminate cleanly within 5 seconds.");
                            DB_EXEC.shutdownNow();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        DB_EXEC.shutdownNow();
                    }
                });
    }

    public static void flushAsync() {
        CompletableFuture.runAsync(DataBaseUtil::flush, DB_EXEC)
                .exceptionally(t -> {
                    LOGGER.error("flushAsync failed: {}", t.getMessage(), t);
                    return null;
                });
    }

    public static void deleteUser(UUID playerUuid) {
        requireConnection("deleteUser", playerUuid.toString());
        CACHE.remove(playerUuid);
        PENDING_WRITES.put(playerUuid, Optional.empty());
    }

    public static void updateUser(PlayerState state) {
        requireConnection("updateUser", state.uuid().toString());
        CACHE.put(state.uuid(), state);
        PENDING_WRITES.put(state.uuid(), Optional.of(state));
    }

    private static Optional<PlayerState> getUser(UUID user) {
        requireConnection("getUser", user.toString());

        String sql = "SELECT uuid, session, hostname, port, complete, transferID, extra FROM player_states WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.toString());

            try (ResultSet set = stmt.executeQuery()) {
                if (!set.next()) return Optional.empty();

                PlayerState state = new PlayerState(
                        UUID.fromString(set.getString("uuid")),
                        UUID.fromString(set.getString("session")),
                        set.getString("hostname"),
                        set.getInt("port"),
                        set.getBoolean("complete"),
                        set.getString("transferID"),
                        set.getBytes("extra")
                );

                CACHE.putIfAbsent(state.uuid(), state);
                return Optional.of(state);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch player state for " + user, e);
        }
    }

    private static void open() {
        if (connection != null) {
            LOGGER.warn("Warning: open() called multiple times!");
            return;
        }

        key = buildKey();

        try {
            connection = DriverManager.getConnection(URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=5000");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("CREATE TABLE IF NOT EXISTS player_states (uuid TEXT PRIMARY KEY NOT NULL, session TEXT NOT NULL, hostname TEXT NOT NULL, port INTEGER NOT NULL, complete BOOL NOT NULL, transferID TEXT NOT NULL, extra BLOB NOT NULL)");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void close() {
        if (connection == null) {
            LOGGER.warn("Warning: close() called multiple times!");
            return;
        }

        try {
            connection.close();
            connection = null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection, ", e);
        }
    }

    private static void flush() {
        requireConnection("flush", "N/A");

        if (PENDING_WRITES.isEmpty()) return;

        Map<UUID, Optional<PlayerState>> batch = new HashMap<>();
        for (UUID uuid : PENDING_WRITES.keySet()) {
            Optional<PlayerState> value = PENDING_WRITES.remove(uuid);
            //noinspection OptionalAssignedToNull
            if (value != null) batch.put(uuid, value);
        }

        if (batch.isEmpty()) return;

        try {
            connection.setAutoCommit(false);
            try {
                for (Map.Entry<UUID, Optional<PlayerState>> entry : batch.entrySet()) {
                    if (entry.getValue().isPresent()) {
                        writeUserToDb(entry.getValue().get());
                    } else {
                        deleteUserFromDb(entry.getKey());
                    }
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                for (Map.Entry<UUID, Optional<PlayerState>> entry : batch.entrySet()) {
                    PENDING_WRITES.putIfAbsent(entry.getKey(), entry.getValue());
                }
                throw new RuntimeException("Failed to flush batch to database", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set autocommit during flush", e);
        }
    }


    private static void writeUserToDb(PlayerState state) {
        String sql = "INSERT INTO player_states (uuid, session, hostname, port, complete, transferID, extra) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET " +
                "session = excluded.session, " +
                "hostname = excluded.hostname, " +
                "port = excluded.port, " +
                "complete = excluded.complete, " +
                "transferID = excluded.transferID, " +
                "extra = excluded.extra";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, state.uuid().toString());
            stmt.setString(2, state.session().toString());
            stmt.setString(3, state.hostname());
            stmt.setInt(4, state.port());
            stmt.setBoolean(5, state.complete());
            stmt.setString(6, state.transferID());
            stmt.setBytes(7, state.extra());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to write player state for " + state.uuid(), e);
        }
    }

    private static void deleteUserFromDb(UUID playerUuid) {
        String sql = "DELETE FROM player_states WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerUuid.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete player state for " + playerUuid, e);
        }
    }

    private static void requireConnection(String method, String context) {
        if (connection == null)
            throw new IllegalStateException("Database connection not initialized before " + method + " called (context: " + context + ")");
    }

    private static SecretKey buildKey() {
        byte[] input = System.getProperty("TransferKey", "").getBytes(StandardCharsets.UTF_8);
        if (input.length < 32) {
            throw new IllegalArgumentException("Supplied TransferKey is too short! Requires at least 32 bytes, found " + input.length);
        }
        return Keys.hmacShaKeyFor(input);
    }
}
