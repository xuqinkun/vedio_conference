package service.messaging;

import common.bean.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import util.Config;

import java.util.Properties;

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
