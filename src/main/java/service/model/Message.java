package service.model;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Message {
    public static final int TYPE_BYTES_NUM = 2;
    public static final int SIZE_BYTES_NUM = 4;
    private MessageType msgType;

    private int msgLen;

    private byte[] data;

    public Message(MessageType msgType, int msgLen, byte[] data) {
        this.msgType = msgType;
        this.msgLen = msgLen;
        this.data = data;
    }

    public MessageType getMsgType() {
        return msgType;
    }

    public int getMsgLen() {
        return msgLen;
    }

    public byte[] getData() {
        return data;
    }

    public ByteBuffer[] serialize() {
        ByteBuffer typeBuffer = ByteBuffer.allocateDirect(TYPE_BYTES_NUM);
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(SIZE_BYTES_NUM);
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(msgLen);

        typeBuffer.putShort(msgType.getVal());
        sizeBuffer.putInt(msgLen);
        dataBuffer.put(data);

        typeBuffer.flip();
        sizeBuffer.flip();
        dataBuffer.flip();
        return new ByteBuffer[]{typeBuffer, sizeBuffer, dataBuffer};
    }

    public static Message fromChannel(final SocketChannel sc) throws IOException {
        if (!sc.isConnected()) {
            return null;
        }

        ByteBuffer[] buffers = channelToBuffers(sc);
        if (buffers == null)
            return null;
        int size = buffers[1].getInt();
        byte[] data = new byte[size];
        ByteBuffer dataBuffer = buffers[2];
        dataBuffer.get(data);

        return new Message(MessageType.valueOf(buffers[0].getShort()), size, data);
    }

    public static ByteBuffer[] channelToBuffers(SocketChannel sc) throws IOException {
        if (!sc.isConnected())
            return null;

        ByteBuffer typeBuffer = ByteBuffer.allocateDirect(TYPE_BYTES_NUM);
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(SIZE_BYTES_NUM);
        ByteBuffer[] buffers = {typeBuffer, sizeBuffer};

        sc.read(buffers);

        sizeBuffer.flip();
        int size = sizeBuffer.getInt();
        ByteBuffer dataBuffer = ByteBuffer.allocateDirect(size);
        while (dataBuffer.position() < size)
            sc.read(dataBuffer);

        typeBuffer.flip();
        sizeBuffer.flip();
        dataBuffer.flip();

        return new ByteBuffer[]{typeBuffer, sizeBuffer, dataBuffer};
    }

    public Image toImage() {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data)) {
            return new Image(bis);
        } catch (IOException ex) {
            return null;
        }
    }

    public static void main(String[] args) {
        Message msg = new Message(MessageType.TEXT, 0, new byte[1]);
        System.out.println(msg.getMsgType() == MessageType.IMAGE);
    }
}
