package util;

import com.github.sarxos.webcam.Webcam;
import controller.JoinMeetingController;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.TaskHolder;
import util.Config;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DeviceUtil {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private static Map<Integer, TaskHolder<FrameGrabber>> grabberMapByDevice = new HashMap<>();

    private static Map<String, TaskHolder<FrameGrabber>> grabberMapByStream = new HashMap<>();

    private static Map<String, TaskHolder<FrameRecorder>> recorderMap = new HashMap<>();

    private static Map<Integer, TaskHolder<Webcam>> webcamMap = new HashMap<>();

    public static void initWebCam(int deviceNum) {
        TaskHolder<Webcam> webcamHolder = getWebcam(deviceNum);
        if (!webcamHolder.isStarted()) {
            Webcam webcam = webcamHolder.getTask();
            if (!webcam.isOpen()) {
                log.warn("Open WebCam. Please wait...");
                webcam.open();
                webcamHolder.setStarted();
                log.warn("WebCam started");
            }
        }
    }

    public static void initRecorder(String outStream) {
        TaskHolder<FrameRecorder> recorderHolder = getRecorder(outStream);
        if (!recorderHolder.isStarted() && !recorderHolder.isSubmitted()) {
            log.warn("Submit recorder startup task. Please wait...");
            recorderHolder.submit();
        }
    }

    public static void initGrabber(String inStream) {
        try {
            getGrabber(inStream);
        } catch (FrameGrabber.Exception e) {
            log.error("Init grabber failed ");
            log.error(e.getCause().toString());
        }
    }

    public static TaskHolder<FrameRecorder> getRecorder(String outStream) {
        if (recorderMap.get(outStream) == null) {
            FrameRecorder recorder = new FFmpegFrameRecorder(outStream, Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
            // Related to clarity 18 is good, 28 is bad.
            recorder.setVideoOption("crf", "28");
//            recorder.setGopSize(Config.getRecorderFrameRate() * 2);
            // H264 causes high latency
//            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setGopSize(2);
            recorder.setVideoBitrate(2000000);
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setFormat("flv");
            recorder.setFrameRate(Config.getRecorderFrameRate());
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setOption("probesize", "10240");  // Max bytes for reading video frame
            recorder.setOption("max_analyze_duration", "5"); // Max duration for analyzing video frame

//            recorder.setSampleRate(44100);
//            recorder.setAudioChannels(2);
//            recorder.setAudioOption("crf","0");
//            recorder.setAudioBitrate(192000);
//            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            TaskHolder<FrameRecorder> taskHolder = new TaskHolder<>(recorder);
            recorderMap.put(outStream, taskHolder);
        }
        return recorderMap.get(outStream);
    }

    public static TaskHolder<FrameGrabber> getGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (grabberMapByDevice.get(deviceNumber) == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(Config.getCaptureImageWidth());
            grabber.setImageHeight(Config.getCaptureImageHeight());
            TaskHolder<FrameGrabber> taskHolder = new TaskHolder<>(grabber);
            grabberMapByDevice.put(deviceNumber, taskHolder);
        }
        return grabberMapByDevice.get(deviceNumber);
    }

    public static TaskHolder<Webcam> getWebcam(int deviceNumber) {
        if (webcamMap.get(deviceNumber) == null) {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(Config.getCaptureImageWidth(), Config.getCaptureImageHeight()));
            webcamMap.put(deviceNumber, new TaskHolder<>(webcam));
        }
        return webcamMap.get(deviceNumber);
    }

    public static TaskHolder<FrameGrabber> getGrabber(String inStream) throws FrameGrabber.Exception {
        if (grabberMapByStream.get(inStream) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inStream);
            TaskHolder<FrameGrabber> taskHolder = new TaskHolder<>(grabber);
            grabberMapByStream.put(inStream, taskHolder);
        }
        return grabberMapByStream.get(inStream);
    }
}
