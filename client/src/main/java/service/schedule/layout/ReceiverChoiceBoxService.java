package service.schedule.layout;

import common.bean.OperationType;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.ChoiceBox;
import service.model.SessionManager;

import static common.bean.OperationType.USER_ADD;
import static common.bean.OperationType.USER_REMOVE;

public class ReceiverChoiceBoxService extends ScheduledService<LayoutChangeSignal> {
    private ChoiceBox<String> receiverChoiceBox;

    public ReceiverChoiceBoxService(ChoiceBox<String> receiverChoiceBox) {
        this.receiverChoiceBox = receiverChoiceBox;
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                OperationType op = newValue.getOp();
                if (op == USER_ADD) {
                    receiverChoiceBox.getItems().add(newValue.getUserName());
                } else {
                    receiverChoiceBox.getItems().remove(newValue.getUserName());
                }
            }
        });
    }

    @Override
    protected Task<LayoutChangeSignal> createTask() {
        return new Task<LayoutChangeSignal>() {
            @Override
            protected LayoutChangeSignal call() throws Exception {
                SessionManager sessionManager = SessionManager.getInstance();
                String currentUser = sessionManager.getCurrentUser().getName();
                for (String userName : sessionManager.getUserList()) {
                    if (!receiverChoiceBox.getItems().contains(userName) && !userName.equals(currentUser)) {
                        updateValue(new LayoutChangeSignal(USER_ADD, userName, null));
                    }
                }
                for (String userName : receiverChoiceBox.getItems()) {
                    if (!userName.equals("All") && !sessionManager.getUserList().contains(userName)) {
                        updateValue(new LayoutChangeSignal(USER_REMOVE, userName, null));
                    }
                }
                return null;
            }
        };
    }
}
