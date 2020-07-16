package javacv;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.SourceDataLine;

public class VideoReceiver extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("CameraReceiver");
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

        String input = "rtmp://localhost:1935/live/room";
        ReceiverTask task = new ReceiverTask(iv, input);
        new Thread(task).start();
    }


    static class ReceiverTask extends Task<Image> {
        ImageView view;
        FFmpegFrameGrabber grabber;
        boolean stopped;
        JavaFXFrameConverter converter;
        long last;

        public ReceiverTask(ImageView view, String input) {
            this.view = view;
            converter = new JavaFXFrameConverter();
            grabber = new FFmpegFrameGrabber(input);
            try {
                grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                grabber.setVideoOption("crf", "18");
                grabber.setVideoBitrate(2000000);
                // 该参数用于降低延迟
                // ultrafast(终极快)提供最少的压缩（低编码器CPU）和最大的视频流大小；
                grabber.setVideoOption("tune", "zerolatency");
                // 提供输出流封装格式(rtmp协议只支持flv封装格式)
                grabber.setFormat("flv");
                // 视频帧率(保证视频质量的情况下最低25，低于25会出现闪屏
                grabber.setFrameRate(30);
                // 关键帧间隔，一般与帧率相同或者是视频帧率的两倍
//                        recorder.setVideoQuality(0);
                grabber.setVideoOption("preset", "ultrafast");
                grabber.start();
            } catch (FrameGrabber.Exception e) {
                e.printStackTrace();
            }
            valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    view.setImage(newValue);
                }
            });
        }

        @Override
        protected Image call() throws Exception {
            last = System.currentTimeMillis();
            while (!stopped) {
                Frame frame = grabber.grab();
                if (frame.imageWidth > 0 && frame.imageHeight > 0) {
                    updateValue(converter.convert(frame));
                    System.out.println(System.currentTimeMillis() - last);
                    last = System.currentTimeMillis();
                }
            }
            return null;
        }
    }
}

