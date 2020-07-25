package service.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ChatMessageContainer {

    private static final ChatMessageContainer INSTANCE = new ChatMessageContainer();

    private BlockingQueue<ChatMessage> messageQueue;

    private ChatMessageContainer() {
        messageQueue = new LinkedBlockingQueue<>();
    }

    public static ChatMessageContainer getInstance() {
        return INSTANCE;
    }

    public void addMessage(ChatMessage msg) {
        messageQueue.offer(msg);
    }

    public ChatMessage next() {
        try {
            return messageQueue.poll(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int size() {
        return messageQueue.size();
    }
}
