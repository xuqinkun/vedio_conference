package service.messaging;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static service.model.MessageType.TEXT;

public class MessageSender {
    private Producer<String, Data> producer;

    private static final MessageSender INSTANCE = new MessageSender();

    private MessageSender() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        producer = new KafkaProducer<>(props, new StringSerializer(), new JsonSerializer<>());
    }

    public static MessageSender getInstance() {
        return INSTANCE;
    }

    public void send(String topic, Data data) throws ExecutionException, InterruptedException {
        Future<RecordMetadata> send = producer.send(new ProducerRecord<>(topic, data.getKey(), data));
        System.out.println(send.get().toString());
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        new MessageSender().send("test", new Data("1", new Date(), TEXT, "hello".getBytes()));
    }
}
