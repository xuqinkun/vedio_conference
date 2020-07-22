package service;

import common.bean.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaService {

    private static final Logger log = LoggerFactory.getLogger(KafkaService.class);

    private final KafkaTemplate kafkaTemplate;

    @Autowired
    public KafkaService(KafkaTemplate kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Message message) {
        kafkaTemplate.send(topic, message);
    }
}
