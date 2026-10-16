package com.clovercraftsmp.clover.companion.protocol;

import com.clovercraftsmp.clover.companion.PlayerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

import java.security.PublicKey;
import java.util.UUID;

public final class CcbotProtocol {
    public static final int HANDSHAKE_INTENT = 2667;

    public interface OutgoingPacket {
        int id();
        void write(FriendlyByteBuf buf);
    }

    // --- Outgoing --- \\
    public record Handshake() implements OutgoingPacket {

        @Override
        public int id() {
            return 0;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeVarInt(0);
            buf.writeUtf("");
            buf.writeShort(0);
            buf.writeVarInt(HANDSHAKE_INTENT);
        }
    }

    public record LoginStart() implements OutgoingPacket {
        @Override
        public int id() {
            return 0;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf("");
            buf.writeUUID(new UUID(0L, 0L));
        }
    }

    public record EncryptionResponse(byte[] sharedSecret, byte[] verifyToken) implements OutgoingPacket {
        @Override
        public int id() {
            return 1;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByteArray(sharedSecret);
            buf.writeByteArray(verifyToken);
        }
    }

    public record Passphrase(String value) implements OutgoingPacket {
        @Override
        public int id() {
            return 1;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(value);
        }
    }

    public record PlayerStateRequest(UUID uuid) implements OutgoingPacket {
        @Override
        public int id() {
            return 2;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeUUID(uuid);
        }
    }

    public record Disconnect() implements OutgoingPacket {
        @Override
        public int id() {
            return 3;
        }

        @Override
        public void write(FriendlyByteBuf buf) {
        }
    }

    // --- Incoming --- \\
    public record EncryptionRequest(PublicKey publicKey, byte[] verifyToken) {
        public static final int ID = 1;

        public static EncryptionRequest read(FriendlyByteBuf buf) {
            buf.readUtf();
            byte[] key = buf.readByteArray();
            byte[] token = buf.readByteArray();
            buf.readBoolean();
            try {
                return new EncryptionRequest(Crypt.byteToPublicKey(key), token);
            } catch (CryptException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public record PassphraseResult(boolean accepted) {
        public static final int ID = 1;

        public static PassphraseResult read(FriendlyByteBuf buf) {
            return new PassphraseResult(buf.readBoolean());
        }
    }

    public record PlayerStateResponse(PlayerState state) {
        public static final int ID = 2;

        public static PlayerStateResponse read(FriendlyByteBuf buf) {
            return new PlayerStateResponse(PlayerState.fromWireId(buf.readVarInt()));
        }
    }
}
