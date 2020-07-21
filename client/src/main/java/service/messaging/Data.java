package service.messaging;

import common.bean.Message;
import service.model.MessageType;

import java.util.Date;

public class Data {
    private String key;

    private Message data;

    public Data() {
    }

    public Data(String key, Message data) {
        this.key = key;
        this.data = data;
    }

    public String getKey() {
        return key;
    }

}
