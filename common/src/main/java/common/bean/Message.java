package common.bean;

import util.JsonUtil;

public class Message {
    private MessageType type;
    private String data;

    public Message() {
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public Message(MessageType type, Object data) {
        this.type = type;
        this.data = JsonUtil.toJsonString(data);
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", data='" + data + '\'' +
                '}';
    }
}
