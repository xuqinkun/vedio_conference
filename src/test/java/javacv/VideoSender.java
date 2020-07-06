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
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.FileNotFoundException;

public class VideoSender extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws FileNotFoundException, FrameGrabber.Exception {
        primaryStage.setTitle("Sender");
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


        VideoSenderService scheduledService = new VideoSenderService();
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
            System.out.println("Start record");
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


    static class VideoSenderService extends ScheduledService<Image> {
        FrameGrabber grabber;
        boolean isRunning;
        JavaFXFrameConverter converter;
        FFmpegFrameRecorder recorder;

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
                        recorder = new FFmpegFrameRecorder("rtmp://localhost:1935/live/room", 640, 480, 2);
//                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                        recorder.setVideoOption("crf", "18");
                        grabber.setVideoOption("crf", "18");
                        recorder.setVideoBitrate(2000000);
                        grabber.setVideoBitrate(2000000);
                        recorder.setVideoOption("tune","zerolatency");
                        recorder.setFormat("flv");
                        recorder.setFrameRate(30);
                        recorder.setGopSize(60);
                        recorder.setAudioOption("crf", "0");
                        grabber.setAudioOption("crf", "0");
                        // 最高质量
                        recorder.setAudioQuality(0);
                        // 音频比特率
                        recorder.setAudioBitrate(192000);
                        grabber.setAudioBitrate(192000);
                        // 音频采样率
                        recorder.setSampleRate(44100);
                        grabber.setSampleRate(44100);
                        // 双通道(立体声)
                        recorder.setAudioChannels(2);
                        grabber.setAudioChannels(2);
                        // 音频编/解码器
                        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                        grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                        recorder.start();



                        grabber.start();
                    }
//                    AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);
////                    Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
////                    // 通过AudioSystem获取本地音频混合器
////                    Mixer mixer = AudioSystem.getMixer(minfoSet[4]);
//                    // 通过设置好的音频编解码器获取数据线信息
//                    DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
//                    try {
//                        // 打开并开始捕获音频
//                        // 通过line可以获得更多控制权
//                        // 获取设备：TargetDataLine line
//                        // =(TargetDataLine)mixer.getLine(dataLineInfo);
//                        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
//                        line.open(audioFormat);
//                        line.start();
//                        // 获得当前音频采样率
//                        int sampleRate = (int) audioFormat.getSampleRate();
//                        // 获取当前音频通道数量
//                        int numChannels = audioFormat.getChannels();
//                        // 初始化音频缓冲区(size是音频采样率*通道数)
//                        int audioBufferSize = sampleRate * numChannels;
//                        byte[] audioBytes = new byte[audioBufferSize];
//
//                        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
//                        exec.scheduleAtFixedRate(() -> {
//                            try {
//                                // 非阻塞方式读取
//                                int nBytesRead = line.read(audioBytes, 0, line.available());
//                                // 因为我们设置的是16位音频格式,所以需要将byte[]转成short[]
//                                int nSamplesRead = nBytesRead / 2;
//                                short[] samples = new short[nSamplesRead];
//
//                                ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
//                                // 将short[]包装到ShortBuffer
//                                ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
//                                // 按通道录制shortBuffer
//                                recorder.recordSamples(sampleRate, numChannels, sBuff);
//                            } catch (FrameRecorder.Exception e) {
//                                e.printStackTrace();
//                            }
//                        }, 0, (long) 1000 / 30, TimeUnit.MILLISECONDS);
//                    } catch (LineUnavailableException e1) {
//                        e1.printStackTrace();
//                    }
                    Frame frame = grabber.grab();
                    recorder.record(frame);
                    return converter.convert(frame);
                }
            };
        }

    }
}

