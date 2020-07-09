package demo.javafx;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

class RefreshTask extends Task<Image> {
    ImageView view;

    public RefreshTask(ImageView view) {
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

public class ImageLoading extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane pane = new AnchorPane();
        ImageView view = new ImageView(new Image("/fxml/img/loading.png"));
        pane.getChildren().add(view);

        RefreshTask task = new RefreshTask(view);
        new Thread(task).start();

        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }
}
