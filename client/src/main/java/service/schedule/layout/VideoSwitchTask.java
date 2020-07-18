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

    private static VideoRecordTask videoRecordTask;

    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

    private static SessionManager sessionManager = SessionManager.getInstance();

    public VideoSwitchTask(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    protected void updateValue(Boolean isOpen) {
        super.updateValue(isOpen);
        GrabberScheduledService grabberScheduledService = sessionManager.getGrabberScheduledService();
        if (grabberScheduledService == null) {
            log.warn("Please wait for system initializing...");
            return;
        }
        if (isOpen) {
            log.debug("Open video");
            if (videoRecordTask == null) {
                User user = sessionManager.getCurrentUser();
                String outputStream = Config.getVideoOutputStream(user.getName());
                videoRecordTask = new VideoRecordTask(outputStream);
                exec.scheduleAtFixedRate(videoRecordTask, 0, Config.getRecorderFrameRate(), TimeUnit.MILLISECONDS);
                grabberScheduledService.start();
            } else if (!grabberScheduledService.isRunning()) {
                grabberScheduledService.restart();
            }
        } else {
            log.debug("Close video");
            grabberScheduledService.cancel();
        }
    }

    @Override
    protected Boolean call() {
        return isOpen;
    }
}
