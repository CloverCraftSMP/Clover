package com.clovercraftsmp.clover.companion;

import com.clovercraftsmp.clover.companion.protocol.PacketStream;
import net.minecraft.util.Crypt;

import javax.crypto.SecretKey;

import static com.clovercraftsmp.clover.companion.protocol.CcbotProtocol.*;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

final class CcbotSession implements Closeable {
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 5000;

    private final PacketStream stream;

    private CcbotSession(PacketStream stream) {
        this.stream = stream;
    }

    static CcbotSession open(String host, int port, String passphrase) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
        socket.setSoTimeout(READ_TIMEOUT_MS);
        PacketStream stream = new PacketStream(socket);
        try {
            stream.send(new Handshake());
            stream.send(new LoginStart());

            EncryptionRequest req = stream.receive(EncryptionRequest.ID, EncryptionRequest::read);
            SecretKey key = Crypt.generateSecretKey();
            stream.send(new EncryptionResponse(
                    Crypt.encryptUsingKey(req.publicKey(), key.getEncoded()),
                    Crypt.encryptUsingKey(req.publicKey(), req.verifyToken())
            ));
            stream.enableEncryption(key);

            stream.send(new Passphrase(passphrase));
            if (!stream.receive(PassphraseResult.ID, PassphraseResult::read).accepted()) {
                throw new CcbotAuthException("Ccbot rejected the passphrase");
            }
            return new CcbotSession(stream);
        } catch (Exception e) {
            stream.close();
            throw e instanceof IOException io ? io : new IOException(e);
        }
    }

    PlayerState queryPlayer(UUID uuid) throws IOException {
        stream.send(new PlayerStateRequest(uuid));
        return stream.receive(PlayerStateResponse.ID, PlayerStateResponse::read).state();
    }

    @Override
    public void close() {
        try { stream.send(new Disconnect()); } catch (IOException ignored) {}
        try { stream.close(); } catch (IOException ignored) {}
    }
}
