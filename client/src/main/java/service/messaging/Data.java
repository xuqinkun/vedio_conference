package service.messaging;

import service.model.MessageType;

import java.util.Date;

public class Data {
    private String key;

    private Date date;

    private MessageType type;

    private byte[] data;

    public Data() {
    }

    public Data(String key, Date date, MessageType type, byte[] data) {
        this.key = key;
        this.date = date;
        this.type = type;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

    public Date getDate() {
        return date;
    }

    public MessageType getType() {
        return type;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Data{" +
                "key='" + key + '\'' +
                ", date=" + date +
                ", type=" + type +
                ", data=" + new String(data) +
                '}';
    }
}
