package service.schedule;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.scene.image.Image;
import service.model.ImageFrame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VideoReceiverService extends ScheduledService<Image> {
    private BlockingQueue<ImageFrame> imageQueue;

    private static VideoReceiverService INSTANCE = new VideoReceiverService();

    public VideoReceiverService() {
        imageQueue = new LinkedBlockingQueue<>();
    }

    public static VideoReceiverService getInstance() {
        return INSTANCE;
    }

    public void addImage(ImageFrame frame) {
        imageQueue.add(frame);
    }

    protected Task<Image> createTask() {
        return new Task<Image>() {
            @Override
            protected Image call() {
                if (imageQueue.size() > 0) {
                    ImageFrame frame = imageQueue.remove();
                    return frame.toImage();
                }
                return null;
            }
        };
    }

}
