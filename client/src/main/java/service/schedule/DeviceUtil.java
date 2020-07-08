package service.schedule;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.util.HashMap;
import java.util.Map;

public class DeviceUtil {
    private static Map<Integer, TaskHolder<FrameGrabber>> grabberMap = new HashMap<>();
    private static Map<String, TaskHolder<FrameRecorder>> recorderMap = new HashMap<>();

    public static TaskHolder<FrameRecorder> getRecorder(String outStream) {
        if (recorderMap.get(outStream) == null) {
            FrameRecorder recorder = new FFmpegFrameRecorder(outStream, 640, 480, 2);
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
            TaskStarter.submit(taskHolder);
        }
        return recorderMap.get(outStream);
    }

    public static TaskHolder<FrameGrabber> getGrabber(int deviceNumber) throws FrameGrabber.Exception {
        if (grabberMap.get(deviceNumber) == null) {
            FrameGrabber grabber = FrameGrabber.createDefault(deviceNumber);
            grabber.setVideoOption("crf", "18");
            grabber.setVideoBitrate(2000000);
            grabber.setAudioOption("crf", "0");
            grabber.setAudioBitrate(192000);
            grabber.setSampleRate(44100);
            grabber.setAudioChannels(2);
            grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
            TaskHolder<FrameGrabber> taskHolder = new TaskHolder<>(grabber);
            grabberMap.put(deviceNumber, taskHolder);
            TaskStarter.submit(taskHolder);
        }
        return grabberMap.get(deviceNumber);
    }
}
