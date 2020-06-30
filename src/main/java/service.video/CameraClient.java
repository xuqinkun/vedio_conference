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
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import service.schedule.VideoReceiverService;
import service.schedule.VideoSenderService;

public class CameraClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws FrameRecorder.Exception, FrameGrabber.Exception {
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

        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.show();

        VideoReceiverService receiverService = new VideoReceiverService("rtmp://localhost:1935/live/room1");
//        VideoReceiverService receiverService = new VideoReceiverService(8888);
        receiverService.setRestartOnFailure(true);
        receiverService.setMaximumFailureCount(4);
        receiverService.setDelay(Duration.millis(0));
        receiverService.setPeriod(Duration.millis(20));

        receiverService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv2.setImage(newValue);
            }
        });

        VideoSenderService senderService = new VideoSenderService("rtmp://localhost:1935/live/room2");
//        VideoSenderService senderService = new VideoSenderService(8888);

        senderService.setRestartOnFailure(true);
        senderService.setMaximumFailureCount(4);
        senderService.setDelay(Duration.millis(0));
        senderService.setPeriod(Duration.millis(20));

        startBtn.setOnAction(event -> {
            senderService.start();
            System.out.println("Start sender service");
            receiverService.start();
            System.out.println("Start receiverService");
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

    private void startSender(Button startBtn, Button cancelBtn, Button resetBtn, Button restartBtn, ImageView iv) throws FrameRecorder.Exception {
//        Client client = new Client("localhost", 8888);
//        ExecutorService executor = Executors.newCachedThreadPool();
//        executor.submit(recorder);


    }
}
