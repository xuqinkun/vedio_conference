package service.schedule.layout;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import util.Helper;

public class TimeCounterService extends ScheduledService<String> {

    private long startTime;

    public TimeCounterService(Label label) {
        startTime = System.currentTimeMillis();
        valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                label.setText(newValue);
            }
        });
    }

    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            @Override
            protected String call() {
                return Helper.millisToTime(System.currentTimeMillis() - startTime);
            }
        };
    }
}
