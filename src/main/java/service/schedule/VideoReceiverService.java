package service.schedule;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoReceiverService extends ScheduledService<Image> {
    private BlockingQueue<Image> imageQueue;
    private long lastSet;

    private final static VideoReceiverService INSTANCE = new VideoReceiverService();

    private VideoReceiverService() {
        imageQueue = new LinkedBlockingQueue<>();
        lastSet = System.currentTimeMillis();
    }

    public static VideoReceiverService getInstance() {
        return INSTANCE;
    }

    public void addImage(Image image) {
        imageQueue.add(image);
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                if (imageQueue.size() > 0) {
                    System.out.println("Set image take:" + (System.currentTimeMillis() - lastSet) + "ms");
                    lastSet = System.currentTimeMillis();
                    return imageQueue.remove();
                }
                return null;
            }
        };
    }

}
