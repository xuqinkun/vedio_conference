package service.schedule;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.*;
import org.springframework.scheduling.support.TaskUtils;
import util.Helper;
import util.ImageUtil;

import java.util.HashMap;
import java.util.Map;

public class DeviceUtil {
    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;
    private static Map<Integer, TaskHolder<FrameGrabber>> grabberMapByDevice = new HashMap<>();

    private static Map<String, TaskHolder<FrameGrabber>> grabberMapByStream = new HashMap<>();

    private static Map<String, TaskHolder<FrameRecorder>> recorderMap = new HashMap<>();

    public static TaskHolder<FrameRecorder> getRecorder(String outStream) {
        if (recorderMap.get(outStream) == null) {
            FrameRecorder recorder = new FFmpegFrameRecorder(outStream, IMAGE_WIDTH, IMAGE_HEIGHT, 2);
            recorder.setVideoOption("crf", "18");
            recorder.setGopSize(60);
            recorder.setVideoBitrate(2000000);
            recorder.setVideoOption("tune", "zerolatency");
            recorder.setFormat("flv");
            recorder.setFrameRate(30);
            recorder.setVideoOption("preset", "ultrafast");
            recorder.setOption("probesize", "34");  // Max bytes for reading video frame
            recorder.setOption("max_analyze_duration", "10"); // Max duration for analyzing video frame
            TaskHolder<FrameRecorder> taskHolder = new TaskHolder<>(recorder);
            recorderMap.put(outStream, taskHolder);
        }
        return recorderMap.get(outStream);
    }

    public static TaskHolder<FrameGrabber> getGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (grabberMapByDevice.get(deviceNumber) == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setImageWidth(IMAGE_WIDTH);
            grabber.setImageHeight(IMAGE_HEIGHT);
            grabber.setVideoOption("crf", "18");
            grabber.setVideoBitrate(2000000);
//            grabber.setAudioOption("crf", "0");
//            grabber.setAudioBitrate(192000);
//            grabber.setSampleRate(44100);
//            grabber.setAudioChannels(2);
//            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            grabber.setOption("probesize", "34");  // Max bytes for reading video frame
            grabber.setOption("max_analyze_duration", "10"); // Max duration for analyzing video frame
            TaskHolder<FrameGrabber> taskHolder = new TaskHolder<>(grabber);
            grabberMapByDevice.put(deviceNumber, taskHolder);
        }
        return grabberMapByDevice.get(deviceNumber);
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
