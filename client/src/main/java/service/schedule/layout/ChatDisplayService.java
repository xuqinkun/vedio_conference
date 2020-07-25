package service.schedule.layout;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.ChatMessage;
import service.model.ChatMessageContainer;
import util.LayoutUtil;

public class ChatDisplayService extends ScheduledService<VBox> {
    private static final Logger log = LoggerFactory.getLogger(ChatDisplayService.class);

    private VBox chatBox;

    public ChatDisplayService(ScrollPane chatBoxScrollPane) {
        this.chatBox = (VBox) chatBoxScrollPane.getContent();
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chatBox.getChildren().add(newValue);
                chatBoxScrollPane.setVvalue(chatBoxScrollPane.getVmax());
            }
        });
        exceptionProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                log.error(newValue.getMessage());
            }
        });
    }

    @Override
    protected Task<VBox> createTask() {
        return new Task<VBox>() {
            @Override
            protected VBox call() throws Exception {
                Stage chatStage = (Stage) chatBox.getScene().getWindow();
                if (chatStage == null || !chatStage.isShowing()) {
                    return null;
                }
                ChatMessage msg = ChatMessageContainer.getInstance().next();
                return msg == null ? null : LayoutUtil.drawChatItemBox(chatBox.getWidth() - 10, msg, false);
            }
        };
    }
}
