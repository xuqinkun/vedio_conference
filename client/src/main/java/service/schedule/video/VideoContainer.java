package service.schedule.video;

import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoContainer {
    private static final Logger LOG = LoggerFactory.getLogger(VideoContainer.class);

    private static final VideoContainer INSTANCE = new VideoContainer();

    private final BlockingQueue<BufferedImage> imageQueue;

    private final BlockingQueue<Frame> frameQueue;

    private final boolean useWebcam;

    private VideoContainer() {
        imageQueue = new LinkedBlockingQueue<>();
        frameQueue = new LinkedBlockingQueue<>();
        useWebcam = Config.useWebcam();
    }

    public static VideoContainer getInstance() {
        return INSTANCE;
    }

    public void addImage(BufferedImage img) {
        try {
            imageQueue.offer(img);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    public void addFrame(Frame frame) {
        frameQueue.offer(frame);
    }

    public Frame getFrame() {
        Frame frame;
        try {
            if (useWebcam) {
                BufferedImage image = imageQueue.take();
                frame = ImageUtil.convert(image);
            } else {
                frame = frameQueue.take();
            }
        } catch (InterruptedException e) {
            LOG.error(e.getMessage());
            frame = null;
        }
        return frame;
    }
}
