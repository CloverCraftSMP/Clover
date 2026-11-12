package com.clovercraftsmp.clover.transfer;

import com.clovercraftsmp.clover.util.DataBaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class FrameStore {
    public enum Role { SENT, RECEIVED }

    public record StoredFrame(TransferState.Frame frame, Role role, boolean popped) {
        public TransferState.Session session() { return frame.session(); }
        public int depth() { return frame.depth(); }
        public UUID frameId() { return frame.frameId(); }
    }

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS transfer_frames ("
            + "uuid TEXT NOT NULL, frame_id TEXT NOT NULL, role TEXT NOT NULL, "
            + "root_id TEXT NOT NULL, session TEXT NOT NULL, session_created INTEGER NOT NULL, depth INTEGER NOT NULL, "
            + "from_id TEXT NOT NULL, from_host TEXT NOT NULL, from_port INTEGER NOT NULL, "
            + "to_id TEXT NOT NULL, to_host TEXT NOT NULL, to_port INTEGER NOT NULL, "
            + "popped BOOL NOT NULL, extra BLOB NOT NULL, "
            + "PRIMARY KEY (uuid, frame_id, role))";

    private static final String SELECT = "SELECT * FROM transfer_frames WHERE uuid = ?";

    private static final String DELETE = "DELETE FROM transfer_frames WHERE uuid = ?";

    private static final String INSERT = "INSERT INTO transfer_frames "
            + "(uuid, frame_id, role, root_id, session, session_created, depth, from_id, from_host, from_port, "
            + "to_id, to_host, to_port, popped, extra) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private FrameStore() {}

    public static void init() {
        DataBaseUtil.registerOpenHook(FrameStore::createTable);
    }

    public static CompletableFuture<Void> editAsync(UUID uuid, Consumer<List<StoredFrame>> edit) {
        return DataBaseUtil.runAsync("edit transfer frames " + uuid, connection -> {
            List<StoredFrame> frames = load(connection, uuid);
            edit.accept(frames);
            replace(connection, uuid, frames);
        });
    }

    private static void createTable(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE);
        }
    }

    static List<StoredFrame> load(Connection connection, UUID uuid) throws SQLException {
        List<StoredFrame> out = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(SELECT)) {
            stmt.setString(1, uuid.toString());
            try (ResultSet set = stmt.executeQuery()) {
                while (set.next()) {
                    byte[] extra = set.getBytes("extra");
                    TransferState.Frame frame = new TransferState.Frame(
                            uuid,
                            new TransferState.Session(set.getString("root_id"), UUID.fromString(set.getString("session")),
                                    set.getLong("session_created")),
                            set.getInt("depth"),
                            UUID.fromString(set.getString("frame_id")),
                            new TransferState.Node(set.getString("from_id"), set.getString("from_host"), set.getInt("from_port")),
                            new TransferState.Node(set.getString("to_id"), set.getString("to_host"), set.getInt("to_port")),
                            extra == null ? new byte[0] : extra
                    );
                    out.add(new StoredFrame(frame, Role.valueOf(set.getString("role")), set.getBoolean("popped")));
                }
            }
        }
        return out;
    }

    static void replace(Connection connection, UUID uuid, List<StoredFrame> frames) throws SQLException {
        connection.setAutoCommit(false);
        try (PreparedStatement delete = connection.prepareStatement(DELETE);
             PreparedStatement insert = connection.prepareStatement(INSERT)) {
            delete.setString(1, uuid.toString());
            delete.executeUpdate();

            for (StoredFrame stored : frames) {
                TransferState.Frame frame = stored.frame();
                insert.setString(1, uuid.toString());
                insert.setString(2, frame.frameId().toString());
                insert.setString(3, stored.role().name());
                insert.setString(4, frame.session().rootId());
                insert.setString(5, frame.session().id().toString());
                insert.setLong(6, frame.session().createdAt());
                insert.setInt(7, frame.depth());
                insert.setString(8, frame.from().id());
                insert.setString(9, frame.from().host());
                insert.setInt(10, frame.from().port());
                insert.setString(11, frame.to().id());
                insert.setString(12, frame.to().host());
                insert.setInt(13, frame.to().port());
                insert.setBoolean(14, stored.popped());
                insert.setBytes(15, frame.extra());
                insert.addBatch();
            }

            if (!frames.isEmpty()) insert.executeBatch();
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            try { connection.rollback(); } catch (SQLException re) { e.addSuppressed(re); }
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
