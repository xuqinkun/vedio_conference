package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.MessageType;
import common.bean.User;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.messaging.MessageReceiveTask;
import service.model.SessionManager;
import service.schedule.DeviceStarter;
import service.schedule.audio.AudioPlayerService;
import service.schedule.video.GrabberScheduledService;
import service.schedule.video.VideoPlayerService;
import util.Config;
import util.DeviceManager;
import util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MeetingRoomInitTask extends Task<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(DeviceStarter.class);
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(5);
    private final Map<String, VideoPlayerService> videoPullServiceMap = new HashMap<>();
    private final Map<String, AudioPlayerService> audioPlayerServiceMap = new HashMap<>();
    private Pane userListLayout;

    private ImageView globalView;

    public MeetingRoomInitTask(Pane userListLayout, ImageView globalView) {
        this.userListLayout = userListLayout;
        this.globalView = globalView;
    }

    @Override
    protected void updateValue(Boolean value) {
        super.updateValue(value);
        init();
    }

    private void init() {
        Meeting currentMeeting = sessionManager.getCurrentMeeting();
        if (currentMeeting == null) {
            log.warn("Can't find current meeting info!");
            /* Only for debug */
            User user = new User("aa", "a");
            sessionManager.setCurrentUser(user);
            Meeting meeting = new Meeting();
            meeting.setUuid("test");
            meeting.setOwner(user.getName());
            sessionManager.setCurrentMeeting(meeting);
        }
        initializeDevice();
        sessionManager.setActiveLayout(sessionManager.getCurrentUser().getName());
        /* Display user list */
        initUserList(sessionManager.getCurrentMeeting());
        /* Listen user change (join or leave)*/
        listenUserListChange(sessionManager.getCurrentMeeting());
    }

    private void listenUserListChange(Meeting currentMeeting) {
        MessageReceiveTask task = new MessageReceiveTask(currentMeeting.getUuid());
        new Thread(task).start();
        task.valueProperty().addListener((observable, oldValue, msg) -> {
            if (msg.getType() == MessageType.USER_ADD) {
                User user = JsonUtil.jsonToObject(msg.getData(), User.class);
                addUser(user);
            }
        });
    }

    @Override
    protected Boolean call() {
        return true;
    }

    private void initializeDevice() {
        log.warn("Initializing devices...");
        String username = sessionManager.getCurrentUser().getName();
        // Initialize video grabber, grabber should be started first
        exec.schedule((Runnable) DeviceManager::initGrabber, 0, TimeUnit.MILLISECONDS);
        // Initialize video recorder
        exec.schedule(() -> {
            DeviceManager.initVideoRecorder(Config.getVideoOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio recorder
        exec.schedule(() -> {
            DeviceManager.initAudioRecorder(Config.getAudioOutputStream(username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio target
        exec.schedule(DeviceManager::initAudioTarget, 0, TimeUnit.MILLISECONDS);
        // Initialize audio player
        exec.schedule(DeviceManager::initAudioPlayer, 0, TimeUnit.MILLISECONDS);
    }

    private void initUserList(Meeting currentMeeting) {
        User currentUser = sessionManager.getCurrentUser();
        if (currentMeeting.getOwner().equals(currentUser.getName())) { // Is meeting owner
            addUser(currentUser);
        } else { // None meeting owner
            HttpResult<String> result = HttpClientUtil.getInstance().
                    doPost(UrlMap.getUserListUrl(), currentMeeting.getUuid());
            List<User> userList = JsonUtil.jsonToList(result.getMessage(), User.class);
            log.warn(userList.toString());
            for (User user : userList)
                addUser(user);
        }
    }

    public void addUser(User user) {
        VBox vBox = new VBox();
        vBox.setId(user.getName());
        vBox.setPrefSize(240, 150);
        vBox.setMaxSize(240, 150);
        vBox.setPadding(new Insets(10));
        vBox.setSpacing(5);
        vBox.setAlignment(Pos.CENTER);
        vBox.setStyle("-fx-border-color: #9B9EA4;-fx-background-color: #424446;-fx-border-radius: 5;-fx-background-radius: 5");

        String portrait = user.getPortrait() == null ? Config.getDefaultPortrait() : user.getPortrait();
        Image image = new Image(portrait);
        ImageView localView = new ImageView(image);
        localView.setFitWidth(vBox.getPrefWidth());
        localView.setFitHeight(130);

        Label label = new Label(user.getName());
        label.setStyle("-fx-text-fill: white;-fx-background-color: #000000");
        label.setPrefSize(localView.getFitWidth(), 25);
        label.setAlignment(Pos.CENTER);
        label.setStyle("-fx-text-alignment: center");

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.getChildren().addAll(label);

        vBox.getChildren().addAll(localView, hBox);
        userListLayout.getChildren().add(vBox);

        vBox.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> {
            SessionManager.getInstance().setActiveLayout(user.getName());
        });

        log.warn("User[{}] added", user);

        String userName = user.getName();
        if (!userName.equals(sessionManager.getCurrentUser().getName()) || sessionManager.isDebugMode()) {
            startVideoPlayer(localView, userName);
            startAudioPlayer(userName);
        }
        if (sessionManager.getGrabberScheduledService() == null) {
            GrabberScheduledService grabberScheduledService = new GrabberScheduledService(localView, globalView, userName);
            sessionManager.setGrabberScheduledService(grabberScheduledService);
        }
    }

    private void startAudioPlayer(String userName) {
        AudioPlayerService audioPlayerService;
        if (!audioPlayerServiceMap.containsKey(userName)) {
            audioPlayerService = new AudioPlayerService(Config.getAudioOutputStream(userName));
            audioPlayerServiceMap.put(userName, audioPlayerService);
        } else {
            audioPlayerService = audioPlayerServiceMap.get(userName);
        }
        if (!audioPlayerService.isRunning()) {
            audioPlayerService.restart();
        } else {
            audioPlayerService.start();
        }
    }

    private void startVideoPlayer(ImageView localView, String userName) {
        VideoPlayerService videoPlayerService;
        if (!videoPullServiceMap.containsKey(userName)) {
            videoPlayerService = new VideoPlayerService(Config.getVideoOutputStream(userName),
                    localView, globalView, userName);
            videoPullServiceMap.put(userName, videoPlayerService);
        } else {
            videoPlayerService = videoPullServiceMap.get(userName);
        }
        if (!videoPlayerService.isRunning()) {
            videoPlayerService.restart();
        } else {
            videoPlayerService.start();
        }
    }
}
