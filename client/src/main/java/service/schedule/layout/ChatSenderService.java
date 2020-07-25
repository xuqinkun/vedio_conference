package service.schedule.layout;

import common.bean.Message;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.messaging.MessageSender;
import service.model.ChatMessage;
import service.model.SessionManager;
import util.Helper;
import util.JsonUtil;
import util.LayoutUtil;

import static common.bean.OperationType.CHAT_MESSAGE;

public class ChatSenderService extends Service<VBox> {

    private static final Logger log = LoggerFactory.getLogger(ChatSenderService.class);

    private VBox chatBox;

    private String topic;

    private String text;

    public ChatSenderService(VBox chatBox, String topic, String text) {
        this.chatBox = chatBox;
        this.topic = topic;
        this.text = text;

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chatBox.getChildren().add(newValue);
            }
        });
    }

    @Override
    protected Task<VBox> createTask() {
        return new Task<VBox>() {
            @Override
            protected VBox call() throws Exception {
                if (!StringUtils.isEmpty(text.trim())) {
                    double width = chatBox.getPrefWidth() - 10;
                    String userName = SessionManager.getInstance().getCurrentUser().getName();
                    ChatMessage chat = new ChatMessage(Helper.currentDate(), userName, text);

                    VBox vBox = LayoutUtil.drawChatItemBox(width, chat);

                    MessageSender.getInstance().send(topic, new Message(CHAT_MESSAGE, JsonUtil.toJsonString(chat)));
                    log.warn("Send message[{}] to [{}]", chat, topic);

                    return vBox;
                }
                return null;
            }
        };
    }
}
