package javacv;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.global.opencv_objdetect;
import org.bytedeco.opencv.opencv_core.IplImage;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;

public class RecordCameraDemo extends Application {

    public static void recordCamera(String outputFile, double frameRate)
            throws Exception {
        Loader.load(opencv_objdetect.class);
        FrameGrabber grabber = FrameGrabber.createDefault(0);
        grabber.start();

        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        IplImage grabbedImage = converter.convert(grabber.grab());
        int width = grabbedImage.width();
        int height = grabbedImage.height();

        FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);

        recorder.start();
        long startTime = 0;
        long videoTS = 0;
        CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);
        Frame rotatedFrame;

        while (frame.isVisible() && (grabbedImage = converter.convert(grabber.grab())) != null) {
            rotatedFrame = converter.convert(grabbedImage);
            frame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTS);
            recorder.record(rotatedFrame);
            Thread.sleep(20);
        }
        frame.dispose();
        recorder.stop();
        recorder.release();
        grabber.stop();
    }

    public static void main(String[] args) throws Exception {
        String outputFile = "rtmp://localhost:1935/live/room";
        int frameRate = 400;
        recordCamera(outputFile, frameRate);
//        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        String outputFile = "rtmp://localhost:1935/live/room";
        int frameRate = 400;
//        recordCamera(outputFile, frameRate);

        Loader.load(opencv_objdetect.class);
        FrameGrabber grabber = FrameGrabber.createDefault(0);
        grabber.start();
        OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();
        IplImage grabbedImage = converter.convert(grabber.grab());
        int width = grabbedImage.width();
        int height = grabbedImage.height();

        FrameRecorder recorder = FrameRecorder.createDefault(outputFile, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);

        recorder.start();
        long startTime = 0;
        long videoTS = 0;
//        CanvasFrame frame = new CanvasFrame("camera", CanvasFrame.getDefaultGamma() / grabber.getGamma());
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setAlwaysOnTop(true);
        Frame rotatedFrame;

        FlowPane flowPane = new FlowPane();
        flowPane.setPrefHeight(1000);
        flowPane.setPrefWidth(1000);

        primaryStage.setScene(new Scene(flowPane));
        primaryStage.show();

        while ((grabbedImage = converter.convert(grabber.grab())) != null) {
            ByteBuffer byteBuffer = grabbedImage.imageData().asBuffer();
            byteBuffer.flip();
            byte[] data = new byte[byteBuffer.capacity()];
            byteBuffer.get(data);
            Image image = new Image(new ByteArrayInputStream(data));
            ImageView iv = new ImageView(image);
            flowPane.getChildren().add(iv);

//            rotatedFrame = converter.convert(grabbedImage);
//            frame.showImage(rotatedFrame);
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }
            videoTS = 1000 * (System.currentTimeMillis() - startTime);
            recorder.setTimestamp(videoTS);
//            recorder.record(rotatedFrame);
            Thread.sleep(20);
        }



//        final JFXPanel mainJFXPanel = new JFXPanel();
//        frame.getContentPane().add(mainJFXPanel);
//        Scene scene = new Scene(new Group());
//        mainJFXPanel.setScene(scene);
//        Loader.load(opencv_objdetect.class);


//        frame.dispose();
        recorder.stop();
        recorder.release();
        grabber.stop();

    }
}
