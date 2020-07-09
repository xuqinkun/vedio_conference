package service.schedule;

import javafx.concurrent.Task;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class LoadingTask extends Task<Image> {
    ImageView view;

    public LoadingTask(ImageView view) {
        this.view = view;
        view.setImage(new Image("/fxml/img/loading.png"));
    }

    @Override
    protected Image call() throws Exception {
        while (!isCancelled()) {
            view.setRotate((view.getRotate() + 3) % 360);
            Thread.sleep(20);
        }
        return null;
    }
}
