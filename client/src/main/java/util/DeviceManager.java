package util;

import com.github.sarxos.webcam.Webcam;
import controller.JoinMeetingController;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.schedule.DefaultThreadFactory;
import service.schedule.DeviceHolder;

import javax.sound.sampled.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static util.Config.OPENCV_GRABBER;
import static util.Config.WEBCAM;

public class DeviceManager {

    private static final Logger log = LoggerFactory.getLogger(JoinMeetingController.class);

    private static Map<String, DeviceHolder<FFmpegFrameGrabber>> videoGrabberMap = new HashMap<>();

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
        getFFmpegFrameGrabber(inStream);
    }

    public static void initGrabber() {
        try {
            int captureType = Config.getCaptureType();
            if (captureType == WEBCAM) {
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
            } else if (captureType == OPENCV_GRABBER) {
                log.warn("Initialize OpenCVFrameGrabber");
                getOpenCVFrameGrabber(Config.getCaptureDevice());
            } else {
                log.warn("Initialize FFmpegFrameGrabber");
                getFFmpegFrameGrabber(Config.getCaptureDevice());
            }
        } catch (Exception e) {
            log.error(e.getCause().toString());
        }
    }

    public static DeviceHolder<FrameGrabber> getOpenCVFrameGrabber(int captureDevice) {
        if (frameGrabberHolder == null) {
            FrameGrabber grabber = new OpenCVFrameGrabber(captureDevice);
            grabber.setImageHeight(Config.getCaptureImageHeight());
            grabber.setImageWidth(Config.getCaptureImageWidth());
            frameGrabberHolder = new DeviceHolder<>(grabber, String.format("OpenCvFrameGrabber[%s]", captureDevice));
            frameGrabberHolder.submit(false);
        }
        return frameGrabberHolder;
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
                mSourceLine.start();
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
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream,
                    Config.getCaptureImageWidth(), Config.getCaptureImageHeight());
            recorder.setInterleaved(true);
            // Related to clarity 18 is good, 28 is bad.
            recorder.setVideoOption("crf", "18");
            recorder.setGopSize(Config.getRecorderFrameRate() * 2);
            // H264 causes high latency
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
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
            videoRecorderHolder.submit(false);
        }
        return videoRecorderHolder;
    }

    public synchronized static DeviceHolder<FFmpegFrameRecorder> getAudioRecorder(String outStream) {
        if (audioRecorderHolder == null) {
            log.debug("Create audio recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, Config.getAudioChannels());
            recorder.setInterleaved(true);
            recorder.setFormat("flv");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setGopSize(Config.getRecorderFrameRate() * 2);
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
            audioRecorderHolder.submit(false);
        }
        return audioRecorderHolder;
    }

    public static DeviceHolder<FrameGrabber> getFFmpegFrameGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (frameGrabberHolder == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(Config.getCaptureImageWidth());
            grabber.setImageHeight(Config.getCaptureImageHeight());
            frameGrabberHolder = new DeviceHolder<>(grabber, String.format("FFmpegFrameGrabber[%s]", deviceNumber));
            frameGrabberHolder.submit(false);
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

    public static DeviceHolder<FFmpegFrameGrabber> getFFmpegFrameGrabber(String url) {
        if (videoGrabberMap.get(url) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
            grabber.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            grabber.setOption("max_analyze_duration", "1");
            grabber.setOption("stimeout", String.valueOf(TimeUnit.SECONDS.toMicros(2)));
//            grabber.setTimeout((int) TimeUnit.SECONDS.toMicros(2));
            DeviceHolder<FFmpegFrameGrabber> deviceHolder = new DeviceHolder<>(grabber, String.format("Video Grabber[%s]", url));
            videoGrabberMap.put(url, deviceHolder);
            delayStart(deviceHolder);
        }
        return videoGrabberMap.get(url);
    }

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10, new DefaultThreadFactory("DeviceManager-"));

    public static void delayStart(DeviceHolder<FFmpegFrameGrabber> deviceHolder) {
        scheduler.schedule(() -> {
            /** Submit grabber startup task only if the recorders have been started and the grabber is not started */
            while (audioRecorderHolder == null || !audioRecorderHolder.isStarted()
                    || videoRecorderHolder == null || !videoRecorderHolder.isStarted()) {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            while (!deviceHolder.isStarted()) {
                deviceHolder.submit(true);
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
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
            grabber.setOption("stimeout", String.valueOf(TimeUnit.SECONDS.toMicros(2)));
            DeviceHolder<FFmpegFrameGrabber> deviceHolder = new DeviceHolder<>(grabber, String.format("Audio Grabber[%s]", inStream));
            delayStart(deviceHolder);
            audioGrabberMap.put(inStream, deviceHolder);
        }
        return audioGrabberMap.get(inStream);
    }
}
