package util;

import com.github.sarxos.webcam.Webcam;
import controller.JoinMeetingController;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.TaskHolder;

import javax.sound.sampled.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DeviceManager {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private static Map<String, TaskHolder<FrameGrabber>> grabberMapByStream = new HashMap<>();

    private static TaskHolder<Webcam> webcamHolder;

    private volatile static TaskHolder<FrameGrabber> frameGrabberHolder;

    private volatile static TaskHolder<FFmpegFrameRecorder> videoRecorderHolder;

    private volatile static TaskHolder<FFmpegFrameRecorder> audioRecorderHolder;

    private volatile static TaskHolder<TargetDataLine> targetDataLineHolder;

    public static void initVideoRecorder(String outStream) {
        videoRecorderHolder = getVideoRecorder(outStream);
        if (!videoRecorderHolder.isStarted() && !videoRecorderHolder.isSubmitted()) {
            log.warn("Submit recorder startup task. Please wait...");
            videoRecorderHolder.submit();
        }
    }

    public static void initAudioRecorder(String outStream) {
        audioRecorderHolder = getAudioRecorder(outStream);
        if (!audioRecorderHolder.isStarted() && !audioRecorderHolder.isSubmitted()) {
            log.warn("Submit recorder startup task. Please wait...");
            audioRecorderHolder.submit();
        }
    }

    public static void initGrabber(String inStream) {
        getGrabber(inStream);
    }

    public static void initGrabber() {
        try {
            if (Config.useWebcam()) {
                TaskHolder<Webcam> webcamHolder = getWebcam();
                if (!webcamHolder.isStarted()) {
                    Webcam webcam = webcamHolder.getTask();
                    if (!webcam.isOpen()) {
                        log.warn("Open WebCam. Please wait...");
                        webcam.open();
                        webcamHolder.setStarted();
                        log.warn("WebCam started");
                    }
                }
            } else
                getGrabber(Config.getCaptureDevice());
        } catch (Exception e) {
            log.error(e.getCause().toString());
        }
    }

    public static void initAudioTarget() {
        getTargetDataLineHolder();
    }

    public synchronized static TaskHolder<TargetDataLine> getTargetDataLineHolder() {
        if (targetDataLineHolder == null) {
            AudioFormat audioFormat = new AudioFormat(Config.getAudioSampleRate(), Config.getAudioSampleSize(),
                    Config.getAudioChannels(), true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            try {
                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open();
                targetDataLine.start();
                targetDataLineHolder = new TaskHolder<>(targetDataLine, "TargetDataLine");
                targetDataLineHolder.setStarted();
            } catch (LineUnavailableException e) {
                log.error("Start audio device failed.Cause:{}", e.getCause().toString());
                return null;
            }
        }
        return targetDataLineHolder;
    }

    public synchronized static TaskHolder<FFmpegFrameRecorder> getVideoRecorder(String outStream) {
        if (videoRecorderHolder == null) {
            log.warn("Create video recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
            recorder.setInterleaved(true);
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
            // Max bytes for reading video frame
            recorder.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            recorder.setOption("max_analyze_duration", "1");

            videoRecorderHolder = new TaskHolder<>(recorder, String.format("Video Recorder[%s]", outStream));
            videoRecorderHolder.submit();
        }
        return videoRecorderHolder;
    }

    public synchronized static TaskHolder<FFmpegFrameRecorder> getAudioRecorder(String outStream) {
        if (audioRecorderHolder == null) {
            log.warn("Create audio recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, Config.getAudioChannels());
            recorder.setInterleaved(true);
            recorder.setGopSize(2);
            recorder.setFormat("flv");
            recorder.setFrameRate(Config.getRecorderFrameRate());
            recorder.setSampleRate(Config.getAudioSampleRate());
            recorder.setAudioOption("crf", "0");
            recorder.setAudioBitrate(Config.getAudioBitrate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            // Max bytes for reading video frame
            recorder.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            recorder.setOption("max_analyze_duration", "1");
            audioRecorderHolder = new TaskHolder<>(recorder, String.format("Audio Recorder[%s]", outStream));
            audioRecorderHolder.submit();
        }
        return audioRecorderHolder;
    }

    public static TaskHolder<FrameGrabber> getGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (frameGrabberHolder == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(Config.getCaptureImageWidth());
            grabber.setImageHeight(Config.getCaptureImageHeight());
            frameGrabberHolder = new TaskHolder<>(grabber, String.format("Frame Grabber[%s]", deviceNumber));
            frameGrabberHolder.submit();
        }
        return frameGrabberHolder;
    }

    public static TaskHolder<Webcam> getWebcam() {
        if (webcamHolder == null) {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(Config.getCaptureImageWidth(), Config.getCaptureImageHeight()));
            webcamHolder = new TaskHolder<>(webcam, "WebCam");
        }
        return webcamHolder;
    }

    public static TaskHolder<FrameGrabber> getGrabber(String inStream) {
        if (grabberMapByStream.get(inStream) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inStream);
            TaskHolder<FrameGrabber> taskHolder = new TaskHolder<>(grabber, String.format("Frame Grabber[%s]", inStream));
            grabberMapByStream.put(inStream, taskHolder);
        }
        return grabberMapByStream.get(inStream);
    }
}
