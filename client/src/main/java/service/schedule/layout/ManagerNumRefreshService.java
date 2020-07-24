package service.schedule.layout;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import service.model.SessionManager;

public class ManagerNumRefreshService extends ScheduledService<Integer> {


    public ManagerNumRefreshService(Label managerIconLabel) {
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                managerIconLabel.setText("Manager (" + newValue + ")");
            }
        });
    }

    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                return SessionManager.getInstance().getManagers().size();
            }
        };
    }
}
