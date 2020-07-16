package javacv;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.sound.sampled.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AudioTest {

    public static void main(String[] args) throws InterruptedException, FrameRecorder.Exception, LineUnavailableException, FrameGrabber.Exception {
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        int frameRate = 24;

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("rtmp://localhost:1935/live/room", 640, 480);
//        OpenCVFrameGrabber grabber = OpenCVFrameGrabber.createDefault(0);
//        grabber.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFormat("flv");
        recorder.setSampleRate(44100);
//        recorder.setOption("probesize", "1024");
//        // Max duration for analyzing video frame
//        recorder.setOption("max_analyze_duration", "1");
        recorder.setFrameRate(frameRate);
        recorder.setAudioChannels(2);
        recorder.setAudioOption("crf", "0");
        recorder.setAudioQuality(0);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.start();

        Runnable crabAudio = recordMicroPhone(4, recorder, frameRate);//对应上面的方法体
        ScheduledFuture tasker = exec.scheduleAtFixedRate(crabAudio, 1000 / frameRate, frameRate,
                TimeUnit.MILLISECONDS);
        Thread.sleep(5000 * 1000);
    }

    public static Runnable recordMicroPhone(int audioDevice, FFmpegFrameRecorder recorder, int frameRate) throws FrameRecorder.Exception, LineUnavailableException, FrameGrabber.Exception {
        /**
         * 设置音频编码器 最好是系统支持的格式，否则getLine() 会发生错误
         * 采样率:44.1k;采样率位数:16位;立体声(stereo);是否签名;true:
         * big-endian字节顺序,false:little-endian字节顺序(详见:ByteOrder类)
         */
        AudioFormat audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
        System.out.println("准备开启音频！");
        // 通过AudioSystem获取本地音频混合器信息
//        Mixer.Info[] minfoSet = AudioSystem.getMixerInfo();
//        // 通过AudioSystem获取本地音频混合器
//        Mixer mixer = AudioSystem.getMixer(minfoSet[audioDevice]);
        // 通过设置好的音频编解码器获取数据线信息
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);

        // 打开并开始捕获音频
        // 通过line可以获得更多控制权
        // 获取设备：TargetDataLine targetDataLine
        // =(TargetDataLine)mixer.getLine(dataLineInfo);
        Line line = null;
        try {
            line = AudioSystem.getLine(dataLineInfo);
        } catch (LineUnavailableException e2) {
            System.err.println("开启失败...");
            return null;
        }
//        TargetDataLine targetDataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
        TargetDataLine targetDataLine = (TargetDataLine) line;
        try {
            targetDataLine.open(audioFormat);
        } catch (LineUnavailableException e1) {
            targetDataLine.stop();
            try {
                targetDataLine.open(audioFormat);
            } catch (LineUnavailableException e) {
                System.err.println("按照指定音频编码器打开失败...");
                return null;
            }
        }
        targetDataLine.start();
        System.out.println("已经开启音频！");
        // 获得当前音频采样率
        int sampleRate = (int) audioFormat.getSampleRate();
        // 获取当前音频通道数量
        int numChannels = audioFormat.getChannels();
        // 初始化音频缓冲区(size是音频采样率*通道数)
        int audioBufferSize = sampleRate * numChannels;
        byte[] audioBytes = new byte[audioBufferSize];

        return () -> {
            // 非阻塞方式读取
            int nBytesRead = targetDataLine.read(audioBytes, 0, targetDataLine.available());
            // 因为我们设置的是16位音频格式,所以需要将byte[]转成short[]
            int nSamplesRead = nBytesRead / 2;
            short[] samples = new short[nSamplesRead];
            /**
             * ByteBuffer.wrap(audioBytes)-将byte[]数组包装到缓冲区
             * ByteBuffer.order(ByteOrder)-按little-endian修改字节顺序，解码器定义的
             * ByteBuffer.asShortBuffer()-创建一个新的short[]缓冲区
             * ShortBuffer.get(samples)-将缓冲区里short数据传输到short[]
             */
            ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
            // 将short[]包装到ShortBuffer
            ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);
            // 按通道录制shortBuffer
            try {
                if (recorder.recordSamples(sampleRate, numChannels, sBuff)) {
//                        recorder.flush();
                } else {
                    System.out.println("Failed");
                }
            } catch (FrameRecorder.Exception e) {
                e.printStackTrace();
            }
//                try {
//                    recorder.record(grabber.grab());
//                } catch (FrameRecorder.Exception | FrameGrabber.Exception e) {
//                    e.printStackTrace();
//                }
        };
    }

}
