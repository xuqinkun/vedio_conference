package service.messaging;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class MessageManager {

    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);

    private static final MessageManager INSTANCE = new MessageManager();

    private AdminClient client;

    private MessageManager() {
        Properties prop = new Properties();
        prop.put("bootstrap.servers", Config.getInstance().getKafkaServer());
        client = AdminClient.create(prop);
    }

    public static MessageManager getInstance() {
        return INSTANCE;
    }

    public void deleteTopics(List<String> topicList) {
        log.warn("Delete topics{}", topicList);
        DeleteTopicsResult result = client.deleteTopics(topicList);
        try {
            result.all().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error(e.getMessage());
        }
    }
}
