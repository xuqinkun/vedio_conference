package common.bean;

import util.JsonUtil;

public class Message {
    private OperationType type;
    private String data;

    public Message() {
    }

    public Message(OperationType type, String data) {
        this.type = type;
        this.data = data;
    }

    public Message(OperationType type, Object data) {
        this.type = type;
        this.data = JsonUtil.toJsonString(data);
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
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
