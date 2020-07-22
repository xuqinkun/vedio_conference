package service.schedule.layout;

import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AnimationControlTask extends Task<Double> {

    Pane node;

    public AnimationControlTask(Pane node, Stage stage) {
        this.node = node;
        valueProperty().addListener((observable, oldValue, opacity) -> {
            if (opacity > 0.01) {
                node.setOpacity(opacity);
            } else {
                stage.close();
            }
        });
    }

    @Override
    protected Double call() throws Exception {
        Thread.sleep(2000);
        while (node.getOpacity() > 0.8) {
            double value = node.getOpacity() - 0.01;
            updateValue(value);
            Thread.sleep(10);
        }
        return 0.0;
    }
}
