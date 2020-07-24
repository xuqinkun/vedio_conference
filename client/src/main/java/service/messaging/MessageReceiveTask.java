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
    public static final Config config = Config.getInstance();
    private boolean stopped;
    private String groupName;

    private Consumer<String, Message> consumer;

    public MessageReceiveTask(String topic, String groupName) {
        this.groupName = groupName;
        consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
    }

    public ConsumerFactory<String, Message> getConsumerFactory() {
        Map<String, Object> consumerParams = new HashMap<>();
        consumerParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaServer());
        consumerParams.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        consumerParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        consumerParams.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        consumerParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<Message> jsonDeserializer = new JsonDeserializer<>();
        jsonDeserializer.addTrustedPackages(config.getKafkaTrustedPackages());
        return new DefaultKafkaConsumerFactory<>(consumerParams, new StringDeserializer(), jsonDeserializer);
    }

    @Override
    protected Message call() {
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
