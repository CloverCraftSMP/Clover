package com.clovercraftsmp.clover.util;

import com.clovercraftsmp.clover.Clover;
import net.fabricmc.loader.api.FabricLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataBaseUtil {
    private static final ExecutorService DB_EXEC = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "clover-db");
        t.setDaemon(true);
        return t;
    });

    private static final List<SqlTask> OPEN_HOOKS = new CopyOnWriteArrayList<>();
    private static final List<SqlTask> FLUSH_HOOKS = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean FLUSH_QUEUED = new AtomicBoolean(false);

    private static Connection connection;
    private static volatile boolean open = false;

    private DataBaseUtil() {}

    @FunctionalInterface
    public interface SqlTask {
        void run(Connection connection) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlQuery<T> {
        T apply(Connection connection) throws SQLException;
    }

    public static void registerOpenHook(SqlTask hook) {
        OPEN_HOOKS.add(hook);
    }

    public static void registerFlushHook(SqlTask hook) {
        FLUSH_HOOKS.add(hook);
    }

    public static boolean isOpen() {
        return open;
    }

    public static <T> CompletableFuture<T> supplyAsync(String name, SqlQuery<T> query) {
        return submit(name, () -> query.apply(requireConnection(name)));
    }

    public static CompletableFuture<Void> runAsync(String name, SqlTask task) {
        return submit(name, () -> {
            task.run(requireConnection(name));
            return null;
        });
    }

    public static CompletableFuture<Void> openAsync() {
        return submit("open", () -> {
            open();
            return null;
        });
    }

    public static CompletableFuture<Void> flushAsync() {
        if (!FLUSH_QUEUED.compareAndSet(false, true)) {
            return CompletableFuture.completedFuture(null);
        }
        return submit("flush", () -> {
            FLUSH_QUEUED.set(false);
            flush();
            return null;
        });
    }

    public static CompletableFuture<Void> closeAsync() {
        if (!DataBaseUtil.isOpen()) return CompletableFuture.completedFuture(null);

        open = false;
        CompletableFuture<Void> future = submit("close", () -> {
            flush();
            close();
            return null;
        });
        DB_EXEC.shutdown();
        return future;
    }

    @FunctionalInterface
    private interface DbCallable<T> {
        T call() throws SQLException;
    }

    private static <T> CompletableFuture<T> submit(String name, DbCallable<T> work) {
        CompletableFuture<T> future;
        try {
            future = CompletableFuture.supplyAsync(() -> {
                try {
                    return work.call();
                } catch (SQLException e) {
                    throw new CompletionException(e);
                }
            }, DB_EXEC);
        } catch (RejectedExecutionException e) {
            future = CompletableFuture.failedFuture(e);
        }

        return future.whenComplete((r, t) -> {
            if (t != null) Clover.LOGGER.error("DB task '{}' failed: {}", name, t.getMessage(), t);
        });
    }

    private static Connection requireConnection(String context) {
        if (connection == null) {
            throw new IllegalStateException("Database not open (during " + context + ")");
        }
        return connection;
    }

    private static void open() throws SQLException {
        if (connection != null) {
            Clover.LOGGER.warn("open() called while already open");
            return;
        }

        Connection c = DriverManager.getConnection("jdbc:sqlite:" + FabricLoader.getInstance().getGameDir().resolve("clover.db").toString());
        try {
            try (Statement stmt = c.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=5000");
                stmt.execute("PRAGMA synchronous=NORMAL");
            }
            for (SqlTask hook : OPEN_HOOKS) hook.run(c);
        } catch (SQLException | RuntimeException e) {
            c.close();
            throw e;
        }

        connection = c;
        open = true;
    }

    private static void flush() {
        if (connection == null) return;
        for (SqlTask hook : FLUSH_HOOKS) {
            try {
                hook.run(connection);
            } catch (SQLException | RuntimeException e) {
                Clover.LOGGER.error("Flush hook failed: {}", e.getMessage(), e);
            }
        }
    }

    private static void close() throws SQLException {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } finally {
            connection = null;
        }
    }

}
