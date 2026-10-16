package com.clovercraftsmp.clover.companion.protocol;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.ProtocolException;
import java.net.Socket;
import java.util.function.Function;

public final class PacketStream implements Closeable {
    private final Socket socket;
    private InputStream in;
    private OutputStream out;

    public PacketStream(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedInputStream(socket.getInputStream());
        this.out = new BufferedOutputStream(socket.getOutputStream());
    }

    public void enableEncryption(SecretKey key) throws CryptException {
        in = new BufferedInputStream(new CipherInputStream(in, Crypt.getCipher(Cipher.DECRYPT_MODE, key)));
        out = new BufferedOutputStream(new CipherOutputStream(out, Crypt.getCipher(Cipher.ENCRYPT_MODE, key)));
    }

    public void send(CcbotProtocol.OutgoingPacket packet) throws IOException {
        FriendlyByteBuf body = new FriendlyByteBuf(Unpooled.buffer());
        body.writeVarInt(packet.id());
        packet.write(body);
        FriendlyByteBuf frame = new FriendlyByteBuf(Unpooled.buffer());
        frame.writeVarInt(body.readableBytes());
        frame.writeBytes(body);
        out.write(ByteBufUtil.getBytes(frame));
        out.flush();
    }

    private static final int MAX_PACKET_SIZE = 1 << 16;

    public <T> T receive(int expectedId, Function<FriendlyByteBuf, T> reader) throws IOException {
        int length = readVarInt();
        if (length <= 0 || length > MAX_PACKET_SIZE) throw new ProtocolException("Bad packet length " + length);
        byte[] data = in.readNBytes(length);
        if (data.length != length) throw new EOFException("Ccbot closed the connection");
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
        try {
            int id = buf.readVarInt();
            if (id != expectedId) throw new ProtocolException("Expected packet " + expectedId + ", got " + id);
            return reader.apply(buf);
        } catch (RuntimeException e) {
            throw new IOException("Malformed packet", e);
        }
    }

    private int readVarInt() throws IOException {
        int value = 0;
        for (int shift = 0; shift < 32; shift += 7) {
            int b = in.read();
            if (b == -1) throw new EOFException("Ccbot closed the connection");
            value |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) return value;
        }
        throw new ProtocolException("VarInt too big");
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
