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
import service.schedule.SlowTaskHolder;

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
    public static final Config config = Config.getInstance();

    private static Map<String, SlowTaskHolder<FFmpegFrameGrabber>> videoGrabberMap = new HashMap<>();

    private static Map<String, SlowTaskHolder<FFmpegFrameGrabber>> audioGrabberMap = new HashMap<>();

    private static SlowTaskHolder<Webcam> webcamHolder;

    private volatile static SlowTaskHolder<FrameGrabber> frameGrabberHolder;

    private volatile static SlowTaskHolder<FFmpegFrameRecorder> videoRecorderHolder;

    private volatile static SlowTaskHolder<FFmpegFrameRecorder> audioRecorderHolder;

    private volatile static SlowTaskHolder<TargetDataLine> targetDataLineHolder;

    public static void initVideoRecorder(String outStream) {
        getVideoRecorder(outStream);
    }

    public static void initAudioRecorder(String outStream) {
        getAudioRecorder(outStream);
    }

    public static void initGrabber() {
        try {
            int captureType = config.getCaptureType();
            if (captureType == WEBCAM) {
                SlowTaskHolder<Webcam> webcamHolder = getWebcam();
                if (!webcamHolder.isStarted()) {
                    Webcam webcam = webcamHolder.getContent();
                    if (!webcam.isOpen()) {
                        log.warn("Open WebCam. Please wait...");
                        webcam.open();
                        webcamHolder.setStarted();
                        log.warn("WebCam started");
                    }
                }
            } else if (captureType == OPENCV_GRABBER) {
                getOpenCVFrameGrabber(config.getCaptureDevice());
            } else {
                getFFmpegFrameGrabber(config.getCaptureDevice());
            }
        } catch (Exception e) {
            log.error(e.getCause().toString());
        }
    }

    public synchronized static SlowTaskHolder<FrameGrabber> getOpenCVFrameGrabber(int captureDevice) {
        if (frameGrabberHolder == null) {
            log.warn("Initialize OpenCVFrameGrabber");
            FrameGrabber grabber = new OpenCVFrameGrabber(captureDevice);
            grabber.setImageHeight(config.getCaptureImageHeight());
            grabber.setImageWidth(config.getCaptureImageWidth());
            frameGrabberHolder = new SlowTaskHolder<>(grabber, String.format("OpenCvFrameGrabber[%s]", captureDevice));
            frameGrabberHolder.submit(false);
        }
        return frameGrabberHolder;
    }

    public static void initAudioTarget() {
        SlowTaskHolder<TargetDataLine> dataLineHolder = getTargetDataLineHolder();
        if (dataLineHolder != null && !dataLineHolder.isStarted()) {
            dataLineHolder.getContent().start();
            dataLineHolder.setStarted();
        }
    }

    public static void initAudioPlayer() {
        SlowTaskHolder<SourceDataLine> sourceDataLineHolder = getSourceDataLineHolder();
        if (sourceDataLineHolder != null && !sourceDataLineHolder.isStarted()) {
            sourceDataLineHolder.getContent().start();
            sourceDataLineHolder.setStarted();
        }
    }

    public synchronized static SlowTaskHolder<TargetDataLine> getTargetDataLineHolder() {
        if (targetDataLineHolder == null) {
            AudioFormat audioFormat = new AudioFormat(config.getAudioSampleRate(), config.getAudioSampleSize(),
                    config.getAudioChannels(), true, false);
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
            try {
                TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                targetDataLine.open();
                targetDataLineHolder = new SlowTaskHolder<>(targetDataLine, "TargetDataLine");
            } catch (LineUnavailableException e) {
                log.error("Start audio device failed.Cause:{}", e.getCause().toString());
                return null;
            }
        }
        return targetDataLineHolder;
    }

    private static SlowTaskHolder<SourceDataLine> sourceDataLineHolder;

    public synchronized static SlowTaskHolder<SourceDataLine> getSourceDataLineHolder() {
        AudioFormat audioFormat = new AudioFormat(config.getAudioSampleRate(), config.getAudioSampleSize(),
                config.getAudioChannels(), true, false);
        if (sourceDataLineHolder == null) {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            try {
                SourceDataLine mSourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                mSourceLine.open(audioFormat);
                mSourceLine.start();
                sourceDataLineHolder = new SlowTaskHolder<>(mSourceLine, "Audio player");
            } catch (LineUnavailableException e) {
                e.printStackTrace();
                log.error("Audio player start failed");
                sourceDataLineHolder = null;
            }
        }
        return sourceDataLineHolder;
    }

    public synchronized static SlowTaskHolder<FFmpegFrameRecorder> getVideoRecorder(String outStream) {
        if (videoRecorderHolder == null) {
            log.debug("Create video recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream,
                    config.getCaptureImageWidth(), config.getCaptureImageHeight());
            recorder.setInterleaved(true);
            // Related to clarity 18 is good, 28 is bad.
            recorder.setVideoOption("crf", "18");
            recorder.setGopSize(config.getRecorderFrameRate() * 2);
            // H264 causes high latency
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setVideoBitrate(2000000);
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setFormat("flv");
            recorder.setFrameRate(config.getRecorderFrameRate());
            recorder.setVideoOption("preset", "ultrafast");
            // Max bytes for reading video frame
            recorder.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            recorder.setOption("max_analyze_duration", "1");

            videoRecorderHolder = new SlowTaskHolder<>(recorder, String.format("Video Recorder[%s]", outStream));
            videoRecorderHolder.submit(false);
        }
        return videoRecorderHolder;
    }

    public synchronized static SlowTaskHolder<FFmpegFrameRecorder> getAudioRecorder(String outStream) {
        if (audioRecorderHolder == null) {
            log.debug("Create audio recorder [out={}]", outStream);
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outStream, config.getAudioChannels());
            recorder.setInterleaved(true);
            recorder.setFormat("flv");
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setGopSize(config.getRecorderFrameRate() * 2);
            recorder.setFrameRate(config.getRecorderFrameRate());
            recorder.setSampleRate(config.getAudioSampleRate());
            recorder.setAudioOption("crf", "0");
            recorder.setAudioBitrate(config.getAudioBitrate());
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            // High quality, high latency
//            recorder.setAudioQuality(0);
            // Max bytes for reading video frame
            recorder.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            recorder.setOption("max_analyze_duration", "1");
            audioRecorderHolder = new SlowTaskHolder<>(recorder, String.format("Audio Recorder[%s]", outStream));
            audioRecorderHolder.submit(false);
        }
        return audioRecorderHolder;
    }

    public synchronized static SlowTaskHolder<FrameGrabber> getFFmpegFrameGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (frameGrabberHolder == null) {
            log.warn("Initialize FFmpegFrameGrabber");
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(config.getCaptureImageWidth());
            grabber.setImageHeight(config.getCaptureImageHeight());
            frameGrabberHolder = new SlowTaskHolder<>(grabber, String.format("FFmpegFrameGrabber[%s]", deviceNumber));
            frameGrabberHolder.submit(false);
        }
        return frameGrabberHolder;
    }

    public synchronized static SlowTaskHolder<Webcam> getWebcam() {
        if (webcamHolder == null) {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(config.getCaptureImageWidth(), config.getCaptureImageHeight()));
            webcamHolder = new SlowTaskHolder<>(webcam, "WebCam");
        }
        return webcamHolder;
    }

    public synchronized static SlowTaskHolder<FFmpegFrameGrabber> getFFmpegFrameGrabber(String url) {
        if (videoGrabberMap.get(url) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(url);
            grabber.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            grabber.setOption("max_analyze_duration", "1");
            grabber.setOption("stimeout", String.valueOf(TimeUnit.SECONDS.toMicros(2)));
//            grabber.setTimeout((int) TimeUnit.SECONDS.toMicros(2));
            SlowTaskHolder<FFmpegFrameGrabber> slowTaskHolder = new SlowTaskHolder<>(grabber, String.format("Video Grabber[%s]", url));
            videoGrabberMap.put(url, slowTaskHolder);
            delayStart(slowTaskHolder);
        }
        return videoGrabberMap.get(url);
    }

    private static final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(10, new DefaultThreadFactory("DeviceManager-"));

    public static void delayStart(SlowTaskHolder<FFmpegFrameGrabber> slowTaskHolder) {
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
            slowTaskHolder.submit(false);
        }, 0, TimeUnit.MILLISECONDS);
    }

    public static SlowTaskHolder<FFmpegFrameGrabber> getAudioGrabber(String inStream) {
        if (audioGrabberMap.get(inStream) == null) {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inStream);
            grabber.setOption("fflags", "nobuffer");
            grabber.setFormat("flv");
            grabber.setSampleRate(config.getAudioSampleRate());
            grabber.setFrameRate(config.getRecorderFrameRate());
            grabber.setAudioChannels(config.getAudioChannels());
            grabber.setAudioOption("crf", "0");
            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            grabber.setOption("probesize", "1024");
            // Max duration for analyzing video frame
            grabber.setOption("max_analyze_duration", "1");
            grabber.setOption("stimeout", String.valueOf(TimeUnit.SECONDS.toMicros(2)));
            SlowTaskHolder<FFmpegFrameGrabber> slowTaskHolder = new SlowTaskHolder<>(grabber, String.format("Audio Grabber[%s]", inStream));
            delayStart(slowTaskHolder);
            audioGrabberMap.put(inStream, slowTaskHolder);
        }
        return audioGrabberMap.get(inStream);
    }
}
