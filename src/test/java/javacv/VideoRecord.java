package javacv;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameRecorder.Exception;
import org.bytedeco.javacv.Java2DFrameConverter;
import util.Helper;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 使用javacv进行录屏
 */
public class VideoRecord {
    //线程池 screenTimer
    private ScheduledThreadPoolExecutor screenTimer;
    //获取屏幕尺寸
    private Rectangle rectangle;
    //视频类 FFmpegFrameRecorder
    private FFmpegFrameRecorder recorder;
    private Robot robot;
    //线程池 exec
    private ScheduledThreadPoolExecutor exec;
    private TargetDataLine line;
    private AudioFormat audioFormat;
    private DataLine.Info dataLineInfo;
    private boolean isHaveDevice = true;
    private long startTime = 0;
    private long videoTS = 0;
    private long pauseTime = 0;
    private double frameRate = 24;

    /**
     * @param output     文件储存路径
     * @param isHaveDevice 传入一个 true
     */
    public VideoRecord(String output, boolean isHaveDevice) {

//        rectangle = new Rectangle(Helper.screenSizeWidth(), Helper.screenSizeHeight());

        recorder = new FFmpegFrameRecorder(output, Helper.screenSizeWidth(), Helper.screenSizeHeight());
        // recorder.setVideoCodec(avcodec.AV_CODEC_ID_AV1); // 13
//         recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4); // 13
//        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setSampleRate(44100);
        recorder.setFrameRate(frameRate);
//        recorder.setVideoQuality(0);
//        recorder.setVideoOption("crf", "23");
//        recorder.setVideoBitrate(100000);
//        recorder.setVideoOption("preset", "ultrafast");
//        recorder.setPixelFormat(0); // yuv420p = 0
        recorder.setAudioChannels(2);
        recorder.setAudioOption("crf", "0");
        // Highest quality
        recorder.setAudioQuality(0);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        try {
            recorder.start();
        } catch (Exception e) {
            System.out.print("*******************************");
        }
        this.isHaveDevice = isHaveDevice;
    }

    /**
     * 开始录制
     */
    public void start() {

        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
        if (pauseTime == 0) {
            pauseTime = System.currentTimeMillis();
        }
        //不录声音了
        // 如果有录音设备则启动录音线程
        if (isHaveDevice) {
//            new Thread(new Runnable() {
//                public void run() {
//
//                }
//            }).start();
            capture();
        }
        // 录屏
//        screenTimer = new ScheduledThreadPoolExecutor(1);
//        screenTimer.scheduleAtFixedRate(new Runnable() {
//
//            public void run() {
//                BufferedImage screenCapture = robot.createScreenCapture(rectangle); // 截屏
//
//                BufferedImage videoImg = new BufferedImage((int) rectangle.getWidth(), (int) rectangle.getHeight(),
//                        BufferedImage.TYPE_3BYTE_BGR); // 声明一个BufferedImage用重绘截图
//                Graphics2D videoGraphics = videoImg.createGraphics();// 创建videoImg的Graphics2D
//                videoGraphics.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
//                videoGraphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
//                        RenderingHints.VALUE_COLOR_RENDER_SPEED);
//                videoGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//                videoGraphics.drawImage(screenCapture, 0, 0, null); // 重绘截图
//
//                //BufferedImage bi = robot.createScreenCapture(new Rectangle(Helper.screenSizeWidth(), Helper.screenSizeHeight()));
//
//                Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
//                Frame frame = java2dConverter.convert(videoImg);
//                try {
//                    videoTS = 1000L
//                            * (System.currentTimeMillis() - startTime - (System.currentTimeMillis() - pauseTime));
//                    // 检查偏移量
//                    if (videoTS > recorder.getTimestamp()) {
//                        recorder.setTimestamp(videoTS);
//                    }
//                    recorder.record(frame); // 录制视频
//                } catch (Exception e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } finally {
//                    // 释放资源
//                    videoGraphics.dispose();
//                    videoGraphics = null;
//                    videoImg.flush();
//                    videoImg = null;
//                    java2dConverter = null;
//                    screenCapture.flush();
//                    screenCapture = null;
//                }
//
//            }
//        }, (int) (1000 / frameRate), (int) (1000 / frameRate), TimeUnit.MILLISECONDS);
    }

    /**
     * 抓取声音
     */
    public void capture() {
        audioFormat = new AudioFormat(44100.0F, 16, 2, true, false);
        dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        try {
            line = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        } catch (LineUnavailableException e1) {
            // TODO Auto-generated catch block
            System.out.println("#################");
        }
        try {
            line.open(audioFormat);
        } catch (LineUnavailableException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        line.start();

        final int sampleRate = (int) audioFormat.getSampleRate();
        final int numChannels = audioFormat.getChannels();

        int audioBufferSize = sampleRate * numChannels;
        final byte[] audioBytes = new byte[audioBufferSize];

        exec = new ScheduledThreadPoolExecutor(1);
        exec.scheduleAtFixedRate(new Runnable() {

            public void run() {
                try {
                    int nBytesRead = line.read(audioBytes, 0, line.available());
                    int nSamplesRead = nBytesRead / 2;
                    short[] samples = new short[nSamplesRead];

                    // Let's wrap our short[] into a ShortBuffer and
                    // pass it to recordSamples
                    ByteBuffer.wrap(audioBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
                    ShortBuffer sBuff = ShortBuffer.wrap(samples, 0, nSamplesRead);

                    // recorder is instance of
                    // org.bytedeco.javacv.FFmpegFrameRecorder
                    recorder.recordSamples(sampleRate, numChannels, sBuff);
                    // System.gc();
                } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                    System.out.println(e);
                }
            }
        }, (int) (1000 / frameRate), (int) (1000 / frameRate), TimeUnit.MILLISECONDS);
    }

    /**
     * 停止
     */
    public void stop() {
        if (null != screenTimer) {
            screenTimer.shutdownNow();
        }
        try {
            recorder.stop();
            recorder.release();
            recorder.close();
            screenTimer = null;
            // screenCapture = null;
            if (isHaveDevice) {
                if (null != exec) {
                    exec.shutdownNow();
                }
                if (null != line) {
                    line.stop();
                    line.close();
                }
                dataLineInfo = null;
                audioFormat = null;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * 暂停
     *
     * @throws Exception
     */
    public void pause() throws Exception {
        screenTimer.shutdownNow();
        screenTimer = null;
        if (isHaveDevice) {
            exec.shutdownNow();
            exec = null;
            line.stop();
            line.close();
            dataLineInfo = null;
            audioFormat = null;
            line = null;
        }
        pauseTime = System.currentTimeMillis();
    }

    /**
     * 截屏
     */
    public static List<File> screenshot() {
        List<File> list = new ArrayList<>();
        try {

            //把抓取到的内容写到一个jpg文件中
            File file = new File("video/new/");
            if (!file.exists()) {
                file.mkdir();
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HHmmss");
            String date = sdf.format(new Date());
            //一秒钟24张图片
            System.out.println("开始截屏");
            for (int a = 0; a <= 100; a++) {
                try {
                    //休眠40毫秒截屏一次，一秒钟25张图
                    Thread.sleep(100);
                } catch (java.lang.Exception e) {

                }
                Robot robot = new Robot();
                //根据指定的区域抓取屏幕的指定区域，1300是长度，800是宽度。
                BufferedImage bi = robot.createScreenCapture(new Rectangle(Helper.screenSizeWidth(), Helper.screenSizeHeight()));
                File f = new File("video/new/" + date + "--" + a + ".jpg");
                ImageIO.write(bi, "jpg", f);
                list.add(f);
            }
            System.out.println("截屏结束");
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return list;
    }


    public static void main(String[] args) throws InterruptedException {
//        String output = "rtmp://localhost:1935/live/room";
        String output = "test.flv";
        VideoRecord record = new VideoRecord(output, true);
        record.start();
        Thread.sleep(10 * 1000);
        record.stop();
    }

}