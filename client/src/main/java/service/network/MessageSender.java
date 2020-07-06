package service.network;

import service.model.Message;

import java.io.IOException;
import java.io.ObjectOutputStream;

import static service.model.MessageType.SENDER;

public class MessageSender extends TcpClient {

    public MessageSender(int port) {
        super(port, SENDER);
    }

    public MessageSender(String host, int port) {
        super(host, port, SENDER);
    }

    @Override
    public void doRun() {
        while (!stopped) {
            try {
                Message message = msgQueue.take();
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(message);
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
