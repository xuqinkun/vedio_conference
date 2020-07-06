package service.messaging;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessageReceiver implements Runnable {
    private Consumer<String, Data> consumer;

    public MessageReceiver() {
        consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Arrays.asList("test", "bar"));
    }

    public ConsumerFactory<String, Data> getConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "test");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<Data> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages("service.messaging");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), jsonDeserializer);
    }

    @Override
    public void run() {
        while (true) {
            ConsumerRecords<String, Data> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Data> record : records) {
                Data data = record.value();
                System.out.printf("offset = %d, key = %s, value = %s%n\n", record.offset(), record.key(), data);
            }
        }
    }
}
