package service.model;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Message {
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
        ByteBuffer typeBuffer = ByteBuffer.allocateDirect(2);
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(4);
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
        ByteBuffer typeBuffer = ByteBuffer.allocateDirect(2);
        ByteBuffer sizeBuffer = ByteBuffer.allocateDirect(4);
        ByteBuffer[] buffers = {typeBuffer, sizeBuffer};

        sc.read(buffers);
        typeBuffer.flip();
        sizeBuffer.flip();

        int size = sizeBuffer.getInt();
        byte[] data = new byte[size];
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        sc.read(dataBuffer);

        return new Message(MessageType.valueOf(typeBuffer.getShort()), size, data);
    }

    public static void main(String[] args) {
        Message msg = new Message(MessageType.TEXT, 0, new byte[1]);
        System.out.println(msg.getMsgType() == MessageType.IMAGE);
    }
}
