package service.schedule.layout;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.model.SessionManager;
import service.schedule.audio.AudioRecordService;
import util.Config;

import static common.bean.OperationType.AUDIO_ON;


public class AudioSwitchService extends Service<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(AudioSwitchService.class);

    private static AudioRecordService audioRecordService;

    private static SessionManager sessionManager = SessionManager.getInstance();

    private Parent audioSwitchBtn;

    private Label label;

    private ImageView audioIcon;

    public AudioSwitchService(Parent audioSwitchBtn) {
        this.audioSwitchBtn = audioSwitchBtn;
        label = (Label)audioSwitchBtn.lookup("#audioBtnLabel");
        audioIcon = (ImageView) audioSwitchBtn.lookup("#audioIcon");

        valueProperty().addListener((observable, oldValue, newValue) -> {
            updateValue(newValue);
        });
    }

    protected void updateValue(Boolean openAudio) {
        Label label = (Label) audioSwitchBtn.getParent().lookup("#audioBtnLabel");
        if (openAudio) {
            if (!sessionManager.hasPermission(AUDIO_ON, true)) {
                return;
            }
            log.debug("Audio open");
            audioIcon.setImage(new Image("/fxml/img/audio_on.png"));
            label.setText("Audio On");
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
            label.setText("Audio Off");
            audioIcon.setImage(new Image("/fxml/img/audio_off.png"));
            audioRecordService.cancel();
        }
    }

    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return label.getText().contains("Off");
            }
        };
    }
}
