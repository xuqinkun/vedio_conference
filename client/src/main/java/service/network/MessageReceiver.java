package service.network;

import service.model.Message;

import java.io.IOException;
import java.io.ObjectInputStream;

import static service.model.MessageType.RECEIVER;

public class MessageReceiver extends TcpClient {

    public MessageReceiver(int port) {
        super(port, RECEIVER);
    }

    public MessageReceiver(String host, int port) {
        super(host, port, RECEIVER);
    }

    @Override
    public void doRun() {
        while (!stopped) {
            try {
                ObjectInputStream oos = new ObjectInputStream(socket.getInputStream());
                Message message = (Message) oos.readObject();
                msgQueue.add(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}
