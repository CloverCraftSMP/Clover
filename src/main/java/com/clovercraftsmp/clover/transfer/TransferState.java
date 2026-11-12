package com.clovercraftsmp.clover.transfer;

import io.jsonwebtoken.*;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class TransferState {
    private static final String TOKEN_SUBJECT = "transfer";
    private static final String TOKEN_ISSUER = "CloverCraft";
    private static final Duration TOKEN_TTL = Duration.ofSeconds(300);

    public static final int MAX_COOKIE_BYTES = 5120;

    private static final int DATA_SPLIT_SIZE = 2500;

    private TransferState() {}

    private static JwtParser parser() {
        return Jwts.parser()
                .verifyWith(TransferConfig.key())
                .requireSubject(TOKEN_SUBJECT)
                .requireIssuer(TOKEN_ISSUER)
                .clockSkewSeconds(5)
                .build();
    }

    private static String sign(JwtBuilder builder) {
        return builder.signWith(TransferConfig.key()).compact();
    }

    private static <T> Optional<T> parse (String token, Function<Claims, T> mapper) {
        JwtParser parser = parser();
        try {
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return Optional.of(mapper.apply(claims));
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private static UUID uuidClaim(Claims claims, String name) {
        return UUID.fromString(claims.get(name, String.class));
    }

    private static JwtBuilder tokenBuilder(UUID uuid, UUID frame) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(TOKEN_SUBJECT)
                .issuer(TOKEN_ISSUER)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(TOKEN_TTL)))
                .claim("uuid", uuid.toString())
                .claim("frame", frame.toString());
    }

    public enum Kind { PUSH, POP }

    public record Node(String id, String host, int port) {
        public Node {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(host, "host");
        }
    }

    public record Session(String rootId, UUID id, long createdAt) {
        public Session {
            Objects.requireNonNull(rootId, "rootId");
            Objects.requireNonNull(id, "id");
        }

        public boolean sameRootAs(Session other) {
            return rootId.equals(other.rootId);
        }
    }

    public record Frame(UUID uuid, Session session, int depth, UUID frameId, Node from, Node to, byte[] extra) {
        public Frame {
            Objects.requireNonNull(uuid, "uuid");
            Objects.requireNonNull(session, "session");
            Objects.requireNonNull(frameId, "frameId");
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
            Objects.requireNonNull(extra, "extra");
        }

        public Frame withExtra(byte[] extra) {
            return new Frame(uuid, session, depth, frameId, from, to, extra);
        }

        public Node destination(Kind kind) {
            return kind == Kind.PUSH ? to : from;
        }

        public Header header(Kind kind) {
            return new Header(kind, uuid, session, depth, frameId, from, to, Math.ceilDiv(extra.length, DATA_SPLIT_SIZE));
        }

        public List<String> sectionTokens() {
            int count = Math.ceilDiv(extra.length, DATA_SPLIT_SIZE);
            List<String> out = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                int start = i * DATA_SPLIT_SIZE;
                int end = Math.min(start + DATA_SPLIT_SIZE, extra.length);
                String token = new SectionState(uuid, frameId, i, Arrays.copyOfRange(extra, start, end)).toToken();
                if (token.length() > MAX_COOKIE_BYTES) {
                    throw new IllegalStateException("Section token is " + token.length()
                            + " bytes; cookie limit is " + MAX_COOKIE_BYTES);
                }
                out.add(token);
            }
            return out;
        }
    }

    public record Header(Kind kind, UUID uuid, Session session, int depth, UUID frameId,
                         Node from, Node to, int sectionCount) {

        public Header {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(uuid, "uuid");
            Objects.requireNonNull(session, "session");
            Objects.requireNonNull(frameId, "frameId");
            Objects.requireNonNull(from, "from");
            Objects.requireNonNull(to, "to");
        }

        public Node recipient() {
            return kind == Kind.PUSH ? to : from;
        }

        public static Optional<Header> fromToken(String token) {
            return parse(token, claims -> new Header(
                    Kind.valueOf(claims.get("kind", String.class)),
                    uuidClaim(claims, "uuid"),
                    new Session(claims.get("root", String.class), uuidClaim(claims, "session"),
                            claims.get("created", Long.class)),
                    claims.get("depth", Integer.class),
                    uuidClaim(claims, "frame"),
                    new Node(claims.get("fromId", String.class), claims.get("fromHost", String.class),
                            claims.get("fromPort", Integer.class)),
                    new Node(claims.get("toId", String.class), claims.get("toHost", String.class),
                            claims.get("toPort", Integer.class)),
                    claims.get("sectionCount", Integer.class)
            ));
        }

        public String toToken() {
            return sign(tokenBuilder(uuid, frameId)
                    .claim("kind", kind.name())
                    .claim("root", session.rootId())
                    .claim("session", session.id().toString())
                    .claim("created", session.createdAt())
                    .claim("depth", depth)
                    .claim("fromId", from.id())
                    .claim("fromHost", from.host())
                    .claim("fromPort", from.port())
                    .claim("toId", to.id())
                    .claim("toHost", to.host())
                    .claim("toPort", to.port())
                    .claim("sectionCount", sectionCount));
        }

        public Optional<Frame> withSections(Map<Integer, SectionState> sections) {
            if (sections.size() != sectionCount) return Optional.empty();

            ByteArrayOutputStream combined = new ByteArrayOutputStream();
            for (int i = 0; i < sectionCount; i++) {
                SectionState section = sections.get(i);
                if (section == null || !section.belongsTo(this)) return Optional.empty();
                combined.writeBytes(section.sectionData());
            }

            return Optional.of(new Frame(uuid, session, depth, frameId, from, to, combined.toByteArray()));
        }
    }

    public record SectionState(UUID uuid, UUID frameId, int sectionID, byte[] sectionData) {
        public boolean belongsTo(Header header) {
            return uuid.equals(header.uuid()) && frameId.equals(header.frameId());
        }

        public static Optional<SectionState> fromToken(String token) {
            return parse(token, claims -> new SectionState(
                    uuidClaim(claims, "uuid"),
                    uuidClaim(claims, "frame"),
                    claims.get("sectionID", Integer.class),
                    Base64.getUrlDecoder().decode(claims.get("sectionData", String.class))
            ));
        }

        public String toToken() {
            return sign(tokenBuilder(uuid, frameId)
                    .claim("sectionID", sectionID)
                    .claim("sectionData", Base64.getUrlEncoder().encodeToString(sectionData)));
        }
    }

}
