package javacv;

import javafx.application.Application;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import javax.sound.sampled.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class VideoReceiver extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("CameraReceiver");
        Pane root = new FlowPane();
        VBox vBox = new VBox(20);
        HBox hBox = new HBox(20);

        Button startBtn = new Button("开始");
        Button cancelBtn = new Button("取消");
        Button resetBtn = new Button("重置");
        Button restartBtn = new Button("重启");
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(200);

        Label l1 = new Label("state");
        Label l2 = new Label("value");
        Label l3 = new Label("title");
        Label l4 = new Label("message");

        ImageView iv = new ImageView(new Image("fxml/img/orange.png"));

        hBox.getChildren().addAll(startBtn, cancelBtn, restartBtn, resetBtn, progressBar, l1, l2, l3, l4);

        vBox.getChildren().addAll(hBox, iv);
        root.getChildren().add(vBox);


        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

        String input = "rtmp://localhost:1935/live/room";
        MyScheduledService scheduledService = new MyScheduledService(root, input);
        //等待5s开始、
        //任务失败后重启
        scheduledService.setRestartOnFailure(true);
        // 任务失败4次后不重启
        scheduledService.setMaximumFailureCount(4);
        //任务启动策略
//        scheduledService.setBackoffStrategy();
        //延迟0毫秒
        scheduledService.setDelay(Duration.millis(0));
        //间隔1000毫秒执行一次
        scheduledService.setPeriod(Duration.millis(20));

        startBtn.setOnAction(event -> {
            scheduledService.start();
            System.out.println("开始");
        });
        cancelBtn.setOnAction(event -> {
            scheduledService.cancel();
            System.out.println("取消");
        });
        resetBtn.setOnAction(event -> {
            scheduledService.reset();
            System.out.println("重置");
        });
        restartBtn.setOnAction(event -> {
            scheduledService.restart();
            System.out.println("重启");
        });

        scheduledService.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                iv.setImage(newValue);
            }
        });
    }


    static class MyScheduledService extends ScheduledService<Image> {

        Pane bp;
        FFmpegFrameGrabber grabber;
        boolean isRunning;
        JavaFXFrameConverter converter;
        SourceDataLine mSourceLine;

        public MyScheduledService(Pane bp, String input) {
            this.bp = bp;
            converter = new JavaFXFrameConverter();
            grabber = new FFmpegFrameGrabber(input);
            AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            try {
                mSourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                mSourceLine.open(audioFormat);
                mSourceLine.start();


                grabber.setFormat("flv");
                grabber.setSampleRate(44100);
                grabber.setFrameRate(24);
                grabber.setAudioChannels(2);
                grabber.setAudioOption("crf", "0");
                grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                grabber.start();
            } catch (LineUnavailableException | FrameGrabber.Exception e) {
                e.printStackTrace();
            }
        }

        protected Task<Image> createTask() {
            return new Task<Image>() {
                @Override
                protected Image call() throws Exception {
                    Frame frame = grabber.grab();
                    Buffer[] samples = frame.samples;
                    if (samples != null && samples.length > 0) {
                        ShortBuffer buffer = (ShortBuffer) samples[0];
                        short[] shorts = new short[buffer.limit()];
                        buffer.get(shorts);
                        byte[] data = new byte[shorts.length * 2];
                        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts);
                        mSourceLine.write(data, 0, data.length);
                    }
//                    if (buffer != null) {
//                        byte[] data = new byte[buffer.limit()];
//                        buffer.get(data);
//                        mSourceLine.write(data, 0, data.length);
//                    }
                    return converter.convert(frame);
                }
            };
        }
    }
}

