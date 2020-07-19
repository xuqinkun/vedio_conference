package service.schedule.layout;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.video.GrabberScheduledService;
import service.schedule.video.VideoRecordTask;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class VideoSwitchTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(VideoSwitchTask.class);

    private boolean isOpen;

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
            if (grabberScheduledService.getState() != State.READY || grabberScheduledService.isRunning()) {
                grabberScheduledService.restart();
            } else {
                grabberScheduledService.start();
            }
        } else {
            log.debug("Close video");
            if (grabberScheduledService.isRunning()) {
                grabberScheduledService.cancel();
            }
        }
    }

    @Override
    protected Boolean call() {
        return isOpen;
    }
}
