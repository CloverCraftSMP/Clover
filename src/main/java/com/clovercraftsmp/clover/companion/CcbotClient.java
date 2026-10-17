package com.clovercraftsmp.clover.companion;

import com.clovercraftsmp.clover.Clover;
import com.clovercraftsmp.clover.companion.protocol.CcbotProtocol;
import com.clovercraftsmp.clover.config.CloverConfig;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public final class CcbotClient {
    private static final long BASE_DELAY_S = 5, MAX_DELAY_S = 300, QUERY_TIMEOUT_S = 8;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Clover-ccbot");
        t.setDaemon(true);
        return t;
    });

    private CcbotSession session;
    private int failures;
    private boolean reconnectScheduled;
    private volatile boolean stopped;

    public static final Set<UUID> APPROVED = ConcurrentHashMap.newKeySet();

    public void start() {
        executor.execute(this::connect);
    }

    public CompletableFuture<CcbotProtocol.PlayerStateResponse> getState(UUID uuid) {
        if (stopped) return CompletableFuture.completedFuture(CcbotProtocol.PlayerStateResponse.FAILED);
        try {
            return CompletableFuture.supplyAsync(() -> query(uuid), executor)
                    .completeOnTimeout(CcbotProtocol.PlayerStateResponse.FAILED, QUERY_TIMEOUT_S, TimeUnit.SECONDS);
        } catch (RejectedExecutionException e) {
            return CompletableFuture.completedFuture(CcbotProtocol.PlayerStateResponse.FAILED);
        }
    }

    public void shutdown() {
        stopped = true;
        executor.execute(() -> {
            if (session != null) {
                session.close();
            }
            session = null;
        });
        executor.shutdown();
    }

    private CcbotProtocol.PlayerStateResponse query(UUID uuid) {
        if (session == null) return CcbotProtocol.PlayerStateResponse.FAILED;
        try {
            return session.queryPlayer(uuid);
        } catch (IOException | RuntimeException e) {
            dropSession(e);
            if (connect()) {
                try {
                    return session.queryPlayer(uuid);
                } catch (IOException | RuntimeException e2) {
                    dropSession(e2);
                }
            }
            return CcbotProtocol.PlayerStateResponse.FAILED;
        }
    }

    private boolean connect() {
        if (stopped) return false;
        if (session != null) return true;
        CloverConfig cfg = CloverConfig.HANDLER.instance();
        try {
            session = CcbotSession.open(cfg.ccbotAddress, cfg.ccbotPort, cfg.ccbotPassphrase);
            failures = 0;
            Clover.LOGGER.info("Connected to ccbot at {}:{}", cfg.ccbotAddress, cfg.ccbotPort);
            return true;
        } catch (CcbotAuthException e) {
            Clover.LOGGER.error("{}; not retrying. Fix ccbotPassphrase in clover.json5 and restart the server.", e.getMessage());
            return false;
        } catch (IOException | RuntimeException e) {
            scheduleReconnect(e);
            return false;
        }
    }

    private void dropSession(Exception cause) {
        if (session != null) session.close();
        session = null;
        scheduleReconnect(cause);
    }

    private void scheduleReconnect(Exception cause) {
        if (stopped || reconnectScheduled) return;
        reconnectScheduled = true;
        long delay = Math.min(MAX_DELAY_S, BASE_DELAY_S << Math.min(failures++, 6));
        Clover.LOGGER.warn("Ccbot connection unavailable ({}), retrying in {}s", cause.getMessage(), delay);
        executor.schedule(() -> {
            reconnectScheduled = false;
            connect();
        }, delay, TimeUnit.SECONDS);
    }
}
