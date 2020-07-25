package service.schedule.layout;

import common.bean.Message;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;
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

    private ChatMessage chatMessage;

    public ChatSenderService(ScrollPane chatBoxScrollPane, ChatMessage chatMessage) {
        this.chatBox = (VBox) chatBoxScrollPane.getContent();
        this.chatMessage = chatMessage;

        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chatBox.getChildren().add(newValue);
                chatBoxScrollPane.setVvalue(chatBoxScrollPane.getVmax());
            }
        });
    }

    @Override
    protected Task<VBox> createTask() {
        return new Task<VBox>() {
            @Override
            protected VBox call() throws Exception {
                double width = chatBox.getPrefWidth() - 10;
                VBox vBox = LayoutUtil.drawChatItemBox(width, chatMessage, true);

                String topic = chatMessage.getReceiver();
                MessageSender.getInstance().send(topic, new Message(CHAT_MESSAGE, JsonUtil.toJsonString(chatMessage)));
                log.warn("Send message[{}] to [{}]", chatMessage, topic);

                return vBox;
            }
        };
    }
}
