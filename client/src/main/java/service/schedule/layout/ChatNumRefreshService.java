package service.schedule.layout;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import service.model.ChatMessageContainer;
import service.model.SessionManager;

public class ChatNumRefreshService extends ScheduledService<Integer> {


    public ChatNumRefreshService(Label chatIconLabel) {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                chatIconLabel.setText("Chat (" + newValue + ")");
            }
        });
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return ChatMessageContainer.getInstance().size();
            }
        };
    }
}
