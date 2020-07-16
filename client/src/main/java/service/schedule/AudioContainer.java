package service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ShortBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AudioContainer {
    private static final Logger LOG = LoggerFactory.getLogger(AudioContainer.class);

    private static final AudioContainer INSTANCE = new AudioContainer();

    private BlockingQueue<ShortBuffer> sampleQueue;

    private AudioContainer() {
        sampleQueue = new LinkedBlockingQueue<>();
    }

    public static AudioContainer getInstance() {
        return INSTANCE;
    }

    public void addSample(ShortBuffer buffer) {
        try {
            sampleQueue.offer(buffer);
        } catch (Exception e) {
            LOG.error(e.getCause().toString());
        }
    }

    public ShortBuffer getSample() {
        if (sampleQueue.size() == 0) {
            return null;
        }
        try {
            return sampleQueue.take();
        } catch (InterruptedException e) {
            LOG.error(e.getCause().toString());
            return null;
        }
    }

}
