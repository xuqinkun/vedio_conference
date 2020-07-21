package service.messaging;

import common.bean.Message;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import util.Config;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static service.model.MessageType.TEXT;

public class MessageSender {
    private Producer<String, Message> producer;

    private static final MessageSender INSTANCE = new MessageSender();

    private MessageSender() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.getInstance().getKafkaServer());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        producer = new KafkaProducer<>(props, new StringSerializer(), new JsonSerializer<>());
    }

    public static MessageSender getInstance() {
        return INSTANCE;
    }

    public void send(String topic, Message data) {
        producer.send(new ProducerRecord<>(topic, data));
    }
}
