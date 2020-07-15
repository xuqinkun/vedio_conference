package service.messaging;

import common.bean.Message;
import javafx.concurrent.Task;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import util.Config;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageReceiveTask extends Task<Message> {
    private boolean stopped;

    private Consumer<String, Message> consumer;

    public MessageReceiveTask(String topic) {
        consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
    }

    public ConsumerFactory<String, Message> getConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.getKafkaServer());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, Config.getKafkaConsumerGroupID());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<Message> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(Config.getKafkaTrustedPackages());
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), jsonDeserializer);
    }

    @Override
    protected Message call() throws Exception {
        while (!stopped) {
            ConsumerRecords<String, Message> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, Message> record : records) {
                Message msg = record.value();
                updateValue(msg);
            }
        }
        return null;
    }

    public void stop() {
        stopped = true;
    }
}
