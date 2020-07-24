package service.messaging;

import common.bean.Meeting;
import common.bean.Message;
import common.bean.OperationType;
import common.bean.User;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import service.model.SessionManager;
import service.schedule.layout.LayoutChangeSignal;
import service.schedule.layout.MeetingRoomControlTask;
import util.Config;
import util.JsonUtil;
import util.SystemUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static common.bean.OperationType.*;

public class MessageReceiveService extends Service<Message> {

    private static final Logger log = LoggerFactory.getLogger(MessageReceiveService.class);

    public static final Config config = Config.getInstance();

    private String groupName;

    private Label hostLabel;

    private Consumer<String, Message> consumer;

    public MessageReceiveService(String topic, Label hostLabel, String groupName, MeetingRoomControlTask task) {
        log.warn("Listen on topic[{}], group[{}]", topic, groupName);
        this.groupName = groupName;
        this.hostLabel = hostLabel;
        consumer = getConsumerFactory().createConsumer();
        consumer.subscribe(Collections.singletonList(topic));
        initListener(task);
    }

    private void initListener(MeetingRoomControlTask task) {
        SessionManager sessionManager = SessionManager.getInstance();
        valueProperty().addListener((observable, oldValue, msg) -> {
            if (msg != null) {
                String data = msg.getData();
                Meeting meeting = sessionManager.getCurrentMeeting();
                OperationType op = msg.getType();
                if (op == USER_ADD) {
                    User user = JsonUtil.jsonToObject(data, User.class);
                    task.addUser(user);
                } else if (op == USER_REMOVE) {
                    User user = JsonUtil.jsonToObject(data, User.class);
                    task.addSignal(new LayoutChangeSignal(USER_REMOVE, user.getName(), null));
                } else if (op == END_MEETING) { // TODO end meeting process
                    log.warn("Meeting is end.");
                } else if (op == HOST_CHANGE) {
                    log.warn("Host change to {}", data);
                    SystemUtil.showSystemInfo(String.format("Host change to %s", data));
                    hostLabel.setText(data);
                    String oldHost = meeting.getHost();
                    meeting.setHost(data);
                    meeting.getManagers().remove(oldHost);
                } else if (op == MANAGER_ADD) {
                    log.warn("Appoint {} as manager", data);
                    meeting.getManagers().add(data);
                } else if (op == MANAGER_REMOVE) {
                    log.warn("Remove manager[{}]", data);
                    meeting.getManagers().remove(data);
                } else if (op == VIDEO_ON) {
                    SystemUtil.showSystemInfo("You are allowed to open video");
                    sessionManager.setVideoCallAllowed(true);
                } else if (op == VIDEO_OFF) {
                    SystemUtil.showSystemInfo("You are forbidden to open video");
                    sessionManager.setVideoCallAllowed(false);
                    task.addSignal(new LayoutChangeSignal(VIDEO_OFF, data, null));
                } else if (op == AUDIO_ON) {
                    SystemUtil.showSystemInfo("You are allowed to open audio");
                    sessionManager.setAudioCallAllowed(true);
                } else if (op == AUDIO_OFF) {
                    SystemUtil.showSystemInfo("You are forbidden to open audio");
                    sessionManager.setAudioCallAllowed(false);
                    task.addSignal(new LayoutChangeSignal(AUDIO_OFF, data, null));
                }
            }
        });
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
    protected Task<Message> createTask() {
        return new Task<Message>() {
            @Override
            protected Message call() throws Exception {
                while (!cancel()) {
                    ConsumerRecords<String, Message> records = consumer.poll(Duration.ofMillis(100));
                    for (ConsumerRecord<String, Message> record : records) {
                        Message msg = record.value();
                        log.warn(msg.toString());
                        updateValue(msg);
                    }
                }
                return null;
            }
        };
    }
}
