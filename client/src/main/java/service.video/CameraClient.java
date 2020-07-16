package service.video;

import javafx.application.Application;
import javafx.scene.Scene;
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
import service.schedule.ImagePushTask;
import service.schedule.ImageRecorder;
import service.schedule.ImageRecorderTask;
import service.schedule.VideoSenderService;
import util.DeviceUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        ImageView iv = new ImageView(new Image("fxml/img/orange.png"));
        ImageView iv2 = new ImageView(new Image("fxml/img/orange.png"));

        iv2.setFitWidth(200);
        iv2.setFitHeight(200);

        vBox.getChildren().addAll(hBox, iv, iv2);
        root.getChildren().add(vBox);

        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.show();

//        VideoReceiverService receiverService = new VideoReceiverService("rtmp://localhost:1935/live/room1");
////        VideoReceiverService receiverService = new VideoReceiverService(8888);
//        receiverService.setRestartOnFailure(true);
//        receiverService.setMaximumFailureCount(4);
//        receiverService.setDelay(Duration.millis(0));
//        receiverService.setPeriod(Duration.millis(20));

//        receiverService.valueProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                iv2.setImage(newValue);
//            }
//        });

        String outputStream = "rtmp://localhost:1935/live/room";
        startSenderService(iv, outputStream);

//        ImagePushTask task = startImagePushTask(iv, outputStream);
//        new Thread(task).start();
//        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
//        exec.scheduleAtFixedRate(task, 0, 20, TimeUnit.MILLISECONDS);
        ImageRecorderTask recorder = new ImageRecorderTask(outputStream);
        DeviceUtil.initRecorder(outputStream);
//        ImageRecorder recorder = new ImageRecorder(outputStream);
        Thread thread = new Thread(recorder);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private ImagePushTask startImagePushTask(ImageView iv, String outputStream) {
        ImagePushTask task = new ImagePushTask(outputStream, iv, null);
        DeviceUtil.initWebCam(0);
        return task;
    }

    private void startSenderService(ImageView iv, String outputStream) throws FrameRecorder.Exception {
        VideoSenderService senderService = new VideoSenderService();
        DeviceUtil.initWebCam(0);
        senderService.setDelay(Duration.millis(0));
        senderService.setPeriod(Duration.millis(20));
        senderService.start();
        senderService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
    }

}
