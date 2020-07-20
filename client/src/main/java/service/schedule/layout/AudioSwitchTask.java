package service.schedule.layout;

import common.bean.User;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.audio.AudioRecordService;
import util.Config;

public class AudioSwitchTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(AudioSwitchTask.class);

    private boolean isOpen;

    private static AudioRecordService audioRecordService;

    private static SessionManager sessionManager = SessionManager.getInstance();

    public AudioSwitchTask(boolean isOpen) {
        this.isOpen = isOpen;
    }

    @Override
    protected void updateValue(Boolean isOpen) {
        super.updateValue(isOpen);
        if (isOpen) {
            log.debug("Audio open");
            if (audioRecordService == null) {
                String username = sessionManager.getCurrentUser().getName();
                String uuid = sessionManager.getCurrentMeeting().getUuid();
                String outputStream = Config.getInstance().getAudioOutputStream(uuid, username);
                audioRecordService = new AudioRecordService(outputStream);
                audioRecordService.start();
            } else if (!audioRecordService.isRunning()) {
                audioRecordService.restart();
            }
        } else {
            log.debug("Audio close");
            audioRecordService.cancel();
        }
    }

    @Override
    protected Boolean call() {
        return isOpen;
    }
}
