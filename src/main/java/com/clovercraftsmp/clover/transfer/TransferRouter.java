package com.clovercraftsmp.clover.transfer;

import com.clovercraftsmp.clover.event.transfer.TransferEvents;
import com.clovercraftsmp.clover.util.DataBaseUtil;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class TransferRouter {
    public record Incoming(TransferState.Kind kind, TransferState.Frame frame) {}

    public sealed interface Decision permits Accept, Send, Reject {}
    public record Accept(@Nullable TransferEvents.Arrival arrival) implements Decision {}
    public record Send(TransferState.Kind kind, TransferState.Frame frame) implements Decision {}
    public record Reject(String reason) implements Decision {}

    private record Route(Decision decision, boolean changed) {}

    private static final ConcurrentHashMap<UUID, TransferState.Frame> CURRENT = new ConcurrentHashMap<>();

    private TransferRouter() {}

    public static @Nullable TransferState.Frame current(UUID uuid) {
        return CURRENT.get(uuid);
    }

    public static void setCurrent(UUID uuid, @Nullable TransferState.Frame frame) {
        if (frame == null) CURRENT.remove(uuid);
        else CURRENT.put(uuid, frame);
    }

    public static CompletableFuture<Decision> routeAsync(UUID uuid, @Nullable Incoming incoming) {
        boolean rejectDirect = TransferConfig.rejectsDirect();
        return DataBaseUtil.supplyAsync("route transfer " + uuid, connection -> {
            List<FrameStore.StoredFrame> frames = FrameStore.load(connection, uuid);
            Route route = route(rejectDirect, frames, incoming);
            if (route.changed()) FrameStore.replace(connection, uuid, frames);
            return route.decision();
        });
    }

    private static Route route(boolean rejectDirect, List<FrameStore.StoredFrame> frames, @Nullable Incoming incoming) {
        if (incoming == null) {
            if (rejectDirect) return new Route(new Reject("Missing or invalid transfer cookie!"), false);
            return new Route(routeLatest(frames), false);
        }

        TransferState.Frame frame = incoming.frame();
        TransferState.Session session = frame.session();

        if (frames.stream().anyMatch(x -> x.session().sameRootAs(session) && x.session().createdAt() > session.createdAt())) {
            return new Route(routeLatest(frames), false);
        }
        boolean changed = frames.removeIf(x -> x.session().sameRootAs(session) && !x.session().equals(session));

        Decision decision;
        if (incoming.kind() == TransferState.Kind.PUSH) {
            if (find(frames, frame.frameId(), FrameStore.Role.RECEIVED) != null) {
                decision = routeByTop(frames, session);
            } else {
                boolean sameSession = frames.stream().anyMatch(x -> x.session().equals(session));
                frames.removeIf(x -> x.session().equals(session) && x.depth() >= frame.depth()
                        && !x.frameId().equals(frame.frameId()));
                frames.add(new FrameStore.StoredFrame(frame, FrameStore.Role.RECEIVED, false));
                changed = true;
                decision = new Accept(new TransferEvents.Arrival(TransferEvents.ArrivalKind.PUSHED_IN, sameSession, frame, frame));
            }
        } else {
            if (find(frames, frame.frameId(), FrameStore.Role.SENT) == null) {
                decision = routeByTop(frames, session);
            } else {
                frames.removeIf(x -> x.session().equals(session) && x.depth() >= frame.depth());
                changed = true;
                FrameStore.StoredFrame top = top(frames, session);
                TransferState.Frame current = top != null && top.role() == FrameStore.Role.RECEIVED && !top.popped() ? top.frame() : null;
                decision = new Accept(new TransferEvents.Arrival(TransferEvents.ArrivalKind.RETURNED, true, frame, current));
            }
        }

        return new Route(decision, changed);
    }

    private static Decision routeLatest(List<FrameStore.StoredFrame> frames) {
        return frames.stream()
                .map(FrameStore.StoredFrame::session)
                .max(Comparator.comparingLong(TransferState.Session::createdAt))
                .map(session -> routeByTop(frames, session))
                .orElse(new Accept(null));
    }

    private static Decision routeByTop(List<FrameStore.StoredFrame> frames, TransferState.Session session) {
        FrameStore.StoredFrame top = top(frames, session);
        if (top == null) return routeLatest(frames);
        if (top.role() == FrameStore.Role.SENT) return new Send(TransferState.Kind.PUSH, top.frame());
        if (top.popped()) return new Send(TransferState.Kind.POP, top.frame());
        return new Accept(new TransferEvents.Arrival(TransferEvents.ArrivalKind.REJOINED, true, top.frame(), top.frame()));
    }

    private static @Nullable FrameStore.StoredFrame top(List<FrameStore.StoredFrame> frames, TransferState.Session session) {
        return frames.stream()
                .filter(x -> x.session().equals(session))
                .max(Comparator.comparingInt(FrameStore.StoredFrame::depth).thenComparing(x -> x.role() == FrameStore.Role.RECEIVED))
                .orElse(null);
    }

    private static @Nullable FrameStore.StoredFrame find(List<FrameStore.StoredFrame> frames, UUID frameId, FrameStore.Role role) {
        for (FrameStore.StoredFrame stored : frames) {
            if (stored.frameId().equals(frameId) && stored.role() == role) return stored;
        }
        return null;
    }
}