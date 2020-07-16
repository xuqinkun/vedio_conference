package service.schedule;

import org.bytedeco.javacv.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ImageUtil;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoContainer {
    private static final Logger LOG = LoggerFactory.getLogger(VideoContainer.class);

    private static final VideoContainer INSTANCE = new VideoContainer();

    private BlockingQueue<BufferedImage> imageQueue;

    private VideoContainer() {
        imageQueue = new LinkedBlockingQueue<>();
    }

    public static VideoContainer getInstance() {
        return INSTANCE;
    }

    public void addImage(BufferedImage img) {
        try {
            imageQueue.offer(img);
        } catch (Exception e) {
            LOG.error(e.getCause().toString());
        }
    }

    public Frame getFrame() {
        try {
            BufferedImage image = imageQueue.take();
            return ImageUtil.convert(image);
        } catch (InterruptedException e) {
            LOG.error(e.getCause().toString());
            return null;
        }
    }
}
