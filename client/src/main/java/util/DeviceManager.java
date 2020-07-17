package util;

import com.github.sarxos.webcam.Webcam;
import controller.JoinMeetingController;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.DeviceHolder;

import javax.sound.sampled.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class DeviceManager {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private static Map<String, DeviceHolder<FrameGrabber>> videoGrabberMap = new HashMap<>();

    private static Map<String, DeviceHolder<FFmpegFrameGrabber>> audioGrabberMap = new HashMap<>();

    private static DeviceHolder<Webcam> webcamHolder;

    private volatile static DeviceHolder<FrameGrabber> frameGrabberHolder;

    private volatile static DeviceHolder<FFmpegFrameRecorder> videoRecorderHolder;

    private volatile static DeviceHolder<FFmpegFrameRecorder> audioRecorderHolder;

    private volatile static DeviceHolder<TargetDataLine> targetDataLineHolder;

    public static void initVideoRecorder(String outStream) {
        getVideoRecorder(outStream);
    }

    public static void initAudioRecorder(String outStream) {
        getAudioRecorder(outStream);
    }

    public static void initGrabber(String inStream) {
        getVideoGrabber(inStream);
    }

    public static void initGrabber() {
        try {
            if (Config.useWebcam()) {
                DeviceHolder<Webcam> webcamHolder = getWebcam();
                if (!webcamHolder.isStarted()) {
                    Webcam webcam = webcamHolder.getDevice();
                    if (!webcam.isOpen()) {
                        log.warn("Open WebCam. Please wait...");
                        webcam.open();
                        webcamHolder.setStarted();
                        log.warn("WebCam started");
                    }
                }
            } else
                getVideoGrabber(Config.getCaptureDevice());
        } catch (Exception e) {
            log.error(e.getCause().toString());
        }
    }

    public static void initAudioTarget() {
        DeviceHolder<TargetDataLine> dataLineHolder = getTargetDataLineHolder();
        if (dataLineHolder != null && !dataLineHolder.isStarted()) {
            dataLineHolder.getDevice().start();
            dataLineHolder.setStarted();
        }
    }

    public static void initAudioPlayer() {
        DeviceHolder<SourceDataLine> sourceDataLineHolder = getSourceDataLineHolder();
        if (sourceDataLineHolder != null && !sourceDataLineHolder.isStarted()) {
            sourceDataLineHolder.getDevice().start();
            sourceDataLineHolder.setStarted();
        }
    }

    public synchronized static DeviceHolder<TargetDataLine> getTargetDataLineHolder() {
        if (targetDataLineHolder == null) {
            AudioFormat audioFormat = new AudioFormat(Config.getAudioSampleRate(), Config.getAudioSampleSize(),
                    Config.getAudioChannels(), true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            try {
                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open();
                targetDataLineHolder = new DeviceHolder<>(targetDataLine, "TargetDataLine");
            } catch (LineUnavailableException e) {
                log.error("Start audio device failed.Cause:{}", e.getCause().toString());
                return null;
            }
        }
        return targetDataLineHolder;
    }

    private static DeviceHolder<SourceDataLine> sourceDataLineHolder;

    public synchronized static DeviceHolder<SourceDataLine> getSourceDataLineHolder() {
        AudioFormat audioFormat = new AudioFormat(Config.getAudioSampleRate(), Config.getAudioSampleSize(),
                Config.getAudioChannels(), true, false);
        if (sourceDataLineHolder == null) {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            try {
                SourceDataLine mSourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                mSourceLine.open(audioFormat);
                sourceDataLineHolder = new DeviceHolder<>(mSourceLine, "Audio player");
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                log.error("Audio player start failed");
                sourceDataLineHolder = null;
            }
        }
        return sourceDataLineHolder;
    }

    public synchronized static DeviceHolder<FFmpegFrameRecorder> getVideoRecorder(String outStream) {
        if (videoRecorderHolder == null) {
            log.debug("Create video recorder [out={}]", outStream);
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

            videoRecorderHolder = new DeviceHolder<>(recorder, String.format("Video Recorder[%s]", outStream));
            videoRecorderHolder.submit();
        }
        return videoRecorderHolder;
    }

    public synchronized static DeviceHolder<FFmpegFrameRecorder> getAudioRecorder(String outStream) {
        if (audioRecorderHolder == null) {
            log.debug("Create audio recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, Config.getAudioChannels());
            recorder.setInterleaved(true);
            recorder.setGopSize(2);
            recorder.setFormat("flv");
            recorder.setFrameRate(Config.getRecorderFrameRate());
            recorder.setSampleRate(Config.getAudioSampleRate());
            recorder.setAudioOption("crf", "0");
            recorder.setAudioBitrate(Config.getAudioBitrate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            // High quality, high latency
//            recorder.setAudioQuality(0);
            // Max bytes for reading video frame
            recorder.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            recorder.setOption("max_analyze_duration", "1");
            audioRecorderHolder = new DeviceHolder<>(recorder, String.format("Audio Recorder[%s]", outStream));
            audioRecorderHolder.submit();
        }
        return audioRecorderHolder;
    }

    public static DeviceHolder<FrameGrabber> getVideoGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (frameGrabberHolder == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(Config.getCaptureImageWidth());
            grabber.setImageHeight(Config.getCaptureImageHeight());
            frameGrabberHolder = new DeviceHolder<>(grabber, String.format("Frame Grabber[%s]", deviceNumber));
            frameGrabberHolder.submit();
        }
        return frameGrabberHolder;
    }

    public static DeviceHolder<Webcam> getWebcam() {
        if (webcamHolder == null) {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(Config.getCaptureImageWidth(), Config.getCaptureImageHeight()));
            webcamHolder = new DeviceHolder<>(webcam, "WebCam");
        }
        return webcamHolder;
    }

    public static DeviceHolder<FrameGrabber> getVideoGrabber(String inStream) {
        if (videoGrabberMap.get(inStream) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inStream);
            grabber.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            grabber.setOption("max_analyze_duration", "1");
            DeviceHolder<FrameGrabber> deviceHolder = new DeviceHolder<>(grabber, String.format("Video Grabber[%s]", inStream));
            deviceHolder.submit();
            videoGrabberMap.put(inStream, deviceHolder);
        }
        return videoGrabberMap.get(inStream);
    }

    public static DeviceHolder<FFmpegFrameGrabber> getAudioGrabber(String inStream) {
        if (audioGrabberMap.get(inStream) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inStream);
            grabber.setOption("fflags", "nobuffer");
            grabber.setFormat("flv");
            grabber.setSampleRate(Config.getAudioSampleRate());
            grabber.setFrameRate(Config.getRecorderFrameRate());
            grabber.setAudioChannels(Config.getAudioChannels());
            grabber.setAudioOption("crf", "0");
            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            grabber.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            grabber.setOption("max_analyze_duration", "1");
            DeviceHolder<FFmpegFrameGrabber> deviceHolder = new DeviceHolder<>(grabber, String.format("Audio Grabber[%s]", inStream));
            deviceHolder.submit();
            audioGrabberMap.put(inStream, deviceHolder);
        }
        return audioGrabberMap.get(inStream);
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        String audioStream = "rtmp://192.168.0.104:1935/live/test-aa-audio";
        DeviceManager.initAudioRecorder(audioStream);
        String videoStream = "rtmp://192.168.0.104:1935/live/test-aa-video";
        DeviceManager.initVideoRecorder(videoStream);
        DeviceHolder<FFmpegFrameRecorder> audioRecorder = DeviceManager.getAudioRecorder(audioStream);
        DeviceHolder<FFmpegFrameRecorder> videoRecorder = DeviceManager.getVideoRecorder(videoStream);
        while (!audioRecorder.isStarted() && videoRecorder.isStarted())
            Thread.sleep(100);
        System.out.println(System.currentTimeMillis() - start);
    }
}
