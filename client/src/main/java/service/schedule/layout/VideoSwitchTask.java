package service.schedule.layout;

import common.bean.User;
import javafx.concurrent.Task;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.ImageLoadingTask;
import service.schedule.video.GrabberScheduledService;
import service.schedule.video.VideoRecordTask;
import util.Config;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VideoSwitchTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(VideoSwitchTask.class);

    private boolean isOpen;

    private static ImageView imageView;

    private static VideoRecordTask videoRecordTask;

    private static GrabberScheduledService grabberScheduledService;

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    private static SessionManager sessionManager = SessionManager.getInstance();

    public VideoSwitchTask(boolean isOpen, ImageView imageView) {
        this.isOpen = isOpen;
        VideoSwitchTask.imageView = imageView;
    }

    @Override
    protected void updateValue(Boolean isOpen) {
        super.updateValue(isOpen);
        if (isOpen) {
            log.warn("Open video");
            if (videoRecordTask == null || grabberScheduledService == null) {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(imageView);
                User user = sessionManager.getCurrentUser();
                String outputStream = Config.getVideoOutputStream(user.getName());
                grabberScheduledService = new GrabberScheduledService(imageView, imageLoadingTask);
                videoRecordTask = new VideoRecordTask(outputStream);
                exec.scheduleAtFixedRate(videoRecordTask, 0, Config.getRecorderFrameRate(), TimeUnit.MILLISECONDS);
                grabberScheduledService.start();
            } else if (!grabberScheduledService.isRunning()) {
                grabberScheduledService.restart();
            }
        } else {
            log.warn("Close video");
            if (grabberScheduledService != null) {
                grabberScheduledService.cancel();
            }
        }
    }

    @Override
    protected Boolean call() {
        return isOpen;
    }
}
