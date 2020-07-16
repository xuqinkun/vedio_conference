package javacv;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;

import java.io.FileNotFoundException;

import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

public class VideoSender extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws FileNotFoundException, FrameGrabber.Exception {
        primaryStage.setTitle("Sender");
        Pane root = new FlowPane();
        VBox vBox = new VBox(20);
        HBox hBox = new HBox(20);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);

        ImageView iv = new ImageView(new Image("fxml/img/orange.png"));

        vBox.getChildren().addAll(hBox, iv);
        root.getChildren().add(vBox);


        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();


        VideoSenderService scheduledService = new VideoSenderService();
        //任务失败后重启
        scheduledService.setRestartOnFailure(true);
        // 任务失败4次后不重启
        scheduledService.setMaximumFailureCount(4);
        //任务启动策略
//        scheduledService.setBackoffStrategy();
        //延迟0毫秒
        scheduledService.setDelay(Duration.millis(0));

        scheduledService.setPeriod(Duration.millis(1));

        scheduledService.start();
        scheduledService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
    }


    static class VideoSenderService extends ScheduledService<Image> {
        FrameGrabber grabber;
        boolean isRunning;
        JavaFXFrameConverter converter;
        FrameRecorder recorder;
        long startTime = 0;

        public VideoSenderService() throws FrameGrabber.Exception {
            isRunning = false;
            converter = new JavaFXFrameConverter();
            grabber = FrameGrabber.createDefault(0);
        }

        protected Task<Image> createTask() {
            return new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    if (!isRunning) {
                        isRunning = true;
                        recorder = FrameRecorder.createDefault("rtmp://localhost:1935/live/room", 640, 480);
//                        recorder.setInterleaved(true);
                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//                        recorder.setVideoOption("crf", "18");
//                        recorder.setVideoBitrate(2000000);
                        // 该参数用于降低延迟
                        // ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；
                        recorder.setVideoOption("preset", "ultrafast");
                        recorder.setVideoOption("tune", "zerolatency");
                        // 提供输出流封装格式(rtmp协议只支持flv封装格式)
                        recorder.setFormat("flv");
                        // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏
                        recorder.setFrameRate(30);
                        // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
                        recorder.setGopSize(5);
//                        recorder.setVideoQuality(0);
//                        recorder.setPixelFormat(AV_PIX_FMT_YUV420P); // yuv420p = 0
                        recorder.setOption("probesize", "1024");  // Max bytes for reading video frame
                        recorder.setOption("max_analyze_duration", "5"); // Max duration for analyzing video frame
                        recorder.start();
                        grabber.start();
                    }
                    if (startTime == 0)
                        startTime = System.currentTimeMillis();
                    long videoTS = 1000 * (System.currentTimeMillis() - startTime);
                    if (videoTS > recorder.getTimestamp()) {
                        recorder.setTimestamp(videoTS);
                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                    return converter.convert(frame);
                }
            };
        }

    }
}

