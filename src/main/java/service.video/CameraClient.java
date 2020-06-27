package service.video;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import service.network.Client;
import service.schedule.VideoReceiverService;
import service.schedule.VideoSenderService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sender");
        Pane root = new FlowPane();
        VBox vBox = new VBox(20);
        HBox hBox = new HBox(20);

        Button startBtn = new Button("Start");
        Button cancelBtn = new Button("Cancel");
        Button resetBtn = new Button("Reset");
        Button restartBtn = new Button("Restart");

        ImageView iv = new ImageView(new Image("img/orange.png"));
        ImageView iv2 = new ImageView(new Image("img/orange.png"));

        iv2.setFitWidth(200);
        iv2.setFitHeight(200);

        hBox.getChildren().addAll(startBtn, cancelBtn, restartBtn, resetBtn);
        vBox.getChildren().addAll(hBox, iv, iv2);
        root.getChildren().add(vBox);

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

        startSender(startBtn, cancelBtn, resetBtn, restartBtn, iv);
        startReceiver(iv2);

    }

    private void startReceiver(ImageView iv2) {

        VideoReceiverService receiverService = new VideoReceiverService();
        receiverService.setRestartOnFailure(true);
        receiverService.setMaximumFailureCount(4);
        receiverService.setDelay(Duration.millis(0));
        receiverService.setPeriod(Duration.millis(20));
        receiverService.start();

        receiverService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv2.setImage(newValue);
            }
        });
    }

    private void startSender(Button startBtn, Button cancelBtn, Button resetBtn, Button restartBtn, ImageView iv) {
        Client client = new Client("localhost", 8888);
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.submit(client);
        VideoSenderService senderService = new VideoSenderService(client);

        senderService.setRestartOnFailure(true);
        senderService.setMaximumFailureCount(4);
        senderService.setDelay(Duration.millis(0));
        senderService.setPeriod(Duration.millis(20));

        startBtn.setOnAction(event -> {
            senderService.start();
            System.out.println("Start record");
        });
        cancelBtn.setOnAction(event -> {
            senderService.cancel();
            System.out.println("Cancel");
        });
        resetBtn.setOnAction(event -> {
            senderService.reset();
            System.out.println("Reset");
        });
        restartBtn.setOnAction(event -> {
            senderService.restart();
            System.out.println("Restart");
        });

        senderService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
    }
}
