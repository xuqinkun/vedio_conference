package service.schedule.layout;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.layout.VBox;
import service.model.ChatMessage;
import service.model.ChatMessageContainer;
import util.LayoutUtil;

public class ChatDisplayService extends ScheduledService<VBox> {
    private VBox chatBox;

    public ChatDisplayService(VBox chatBox) {
        this.chatBox = chatBox;

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
                ChatMessage msg = ChatMessageContainer.getInstance().next();
                return msg == null ? null : LayoutUtil.drawChatItemBox(chatBox.getWidth() - 10, msg);
            }
        };
    }
}
