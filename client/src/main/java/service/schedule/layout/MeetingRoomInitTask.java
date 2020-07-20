package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.MessageType;
import common.bean.User;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.messaging.MessageReceiveTask;
import service.model.SessionManager;
import service.schedule.TaskStarter;
import service.schedule.audio.AudioPlayerService;
import service.schedule.video.GrabberScheduledService;
import service.schedule.video.VideoPlayerService;
import service.schedule.video.VideoRecordTask;
import util.Config;
import util.DeviceManager;
import util.JsonUtil;
import util.ThreadPoolUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.bean.MessageType.USER_ADD;
import static common.bean.MessageType.USER_LEAVE;

class LayoutChangeMessage {
    final MessageType type;
    final String controlID;
    final StackPane pane;

    public LayoutChangeMessage(MessageType type, String controlID, StackPane pane) {
        this.type = type;
        this.controlID = controlID;
        this.pane = pane;
    }
}

public class MeetingRoomInitTask extends Task<LayoutChangeMessage> {

    private static final Logger log = LoggerFactory.getLogger(TaskStarter.class);
    public static final Config config = Config.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ScheduledThreadPoolExecutor exec = ThreadPoolUtil.getScheduledExecutor(5, "MeetingRoomStart");

    private final Map<String, VideoPlayerService> videoPullServiceMap = new HashMap<>();
    private final Map<String, AudioPlayerService> audioPlayerServiceMap = new HashMap<>();

    private ImageView globalView;
    private StackPane lastClicked;
    private VideoRecordTask videoRecordTask;

    public MeetingRoomInitTask(Pane userListLayout, ImageView globalView) {
        this.globalView = globalView;
        initListener(userListLayout);
    }

    private void initListener(Pane userListLayout) {
        valueProperty().addListener((observable, oldValue, layoutChangeMessage) -> {
            if (layoutChangeMessage != null) {
                MessageType type = layoutChangeMessage.type;
                if (type == USER_ADD) {
                    log.warn("USER_ADD[{}]", layoutChangeMessage.controlID);
                    userListLayout.getChildren().add(layoutChangeMessage.pane);
                } else if (type == USER_LEAVE) {
                    log.warn("USER_LEAVE[{}]", layoutChangeMessage.controlID);
                    Node node = userListLayout.lookup("#" + layoutChangeMessage.controlID);
                    userListLayout.getChildren().remove(node);
                }
            }
        });
        exceptionProperty().addListener((observable, oldValue, newValue) -> log.error(newValue.getMessage()));
    }

    private void init() {
        String username = sessionManager.getCurrentUser().getName();
        Meeting currentMeeting = sessionManager.getCurrentMeeting();

        initializeDevice();
        sessionManager.setActiveLayout(username);
        /* Display user list */
        initUserList(currentMeeting);
        /* Listen user change (join or leave)*/
        listenUserListChange(currentMeeting);
        // Initialize recorder
        initRecorder();
    }

    private void initRecorder() {
        if (videoRecordTask == null) {
            String username = sessionManager.getCurrentUser().getName();
            String meetingId = sessionManager.getCurrentMeeting().getUuid();
            String outputStream = config.getVideoOutputStream(meetingId, username);
            videoRecordTask = new VideoRecordTask(outputStream);
            exec.scheduleAtFixedRate(videoRecordTask, 0, config.getRecorderFrameRate(), TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected LayoutChangeMessage call() {
        init();
        return null;
    }

    private void initializeDevice() {
        log.warn("Initializing devices...");
        String username = sessionManager.getCurrentUser().getName();
        String meetingId = sessionManager.getCurrentMeeting().getUuid();
        // Initialize video grabber, grabber should be started first
        exec.schedule(DeviceManager::initGrabber, 0, TimeUnit.MILLISECONDS);
        // Initialize video recorder
        exec.schedule(() -> {
            DeviceManager.initVideoRecorder(config.getVideoOutputStream(meetingId, username));
        }, 0, TimeUnit.MILLISECONDS);
        // Initialize audio recorder
        exec.schedule(() -> {
            DeviceManager.initAudioRecorder(config.getAudioOutputStream(meetingId, username));
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
            for (User user : userList) {
                addUser(user);
            }
        }
    }

    private void listenUserListChange(Meeting currentMeeting) {
        MessageReceiveTask task = new MessageReceiveTask(currentMeeting.getUuid());
        new Thread(task).start();
        task.valueProperty().addListener((observable, oldValue, msg) -> {
            if (msg.getType() == USER_ADD) {
                User user = JsonUtil.jsonToObject(msg.getData(), User.class);
                addUser(user);
            } else if (msg.getType() == USER_LEAVE) {
                User user = JsonUtil.jsonToObject(msg.getData(), User.class);
                updateValue(new LayoutChangeMessage(USER_LEAVE, user.getName(), null));
            }
        });
    }

    public void addUser(User user) {
        sessionManager.addUser(user);

        String userName = user.getName();
        String activeStyle = "-fx-border-color: #00cc66;-fx-border-width: 3;-fx-border-radius: 5;-fx-background-radius: 5";
        String normalStyle = "-fx-border-color: #9B9EA4;-fx-border-width: 3;-fx-background-color: #424446;-fx-border-radius: 5;-fx-background-radius: 5";

        int width = 230;
        int height = 150;
        StackPane stackPane = new StackPane();
        stackPane.setId(user.getName());
        stackPane.setPadding(new Insets(3));
        stackPane.setPrefSize(width, height);
        stackPane.setMaxSize(width, height);
        stackPane.setMinSize(width, height);
        stackPane.setAlignment(Pos.CENTER);

        if (sessionManager.isCurrentUser(userName)) {
            stackPane.setStyle(activeStyle);
            lastClicked = stackPane;
        } else {
            stackPane.setStyle(normalStyle);
        }

        String portrait = user.getPortraitSrc() == null ? config.getDefaultPortraitSrc() : user.getPortraitSrc();
        Image image = new Image(portrait);
        ImageView localView = new ImageView(image);
        localView.setFitWidth(stackPane.getPrefWidth() - 7);
        localView.setFitHeight(stackPane.getPrefHeight() - 7);

        Label label = new Label(user.getName());
        label.setStyle("-fx-text-fill: white;-fx-background-color: #000000");
        label.setPrefSize(localView.getFitWidth(), 20);
        label.setAlignment(Pos.CENTER);
        label.setOpacity(0.3);
        StackPane.setAlignment(label, Pos.BOTTOM_CENTER);

        stackPane.getChildren().addAll(localView, label);

        stackPane.setOnMouseClicked(event -> {
            log.debug("Clicked:{}", stackPane.getId());
            if (event.getClickCount() == 2) {
                SessionManager.getInstance().setActiveLayout(user.getName());
                stackPane.setStyle(activeStyle);
                if (lastClicked != stackPane) {
                    lastClicked.setStyle(normalStyle);
                    lastClicked = stackPane;
                    globalView.setVisible(false);
                }
            }
        });

        updateValue(new LayoutChangeMessage(USER_ADD, userName, stackPane));
        try {
            // Waiting for layout loading
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String meetingId = sessionManager.getCurrentMeeting().getUuid();
        log.warn("User[{}] added", user);
        if (!sessionManager.isCurrentUser(userName)) {
            exec.submit(() -> {
                startVideoPlayer(localView, meetingId, userName);
                startAudioPlayer(meetingId, userName);
            });
        } else if (sessionManager.isDebugMode()) {
            startAudioPlayer(meetingId, userName);
        }
        if (sessionManager.getGrabberScheduledService() == null && sessionManager.isCurrentUser(userName)) {
            GrabberScheduledService grabberScheduledService = new GrabberScheduledService(localView, globalView, userName);
            sessionManager.setGrabberScheduledService(grabberScheduledService);
        }
    }

    private void startAudioPlayer(String meetingId, String userName) {
        AudioPlayerService audioPlayerService;
        if (!audioPlayerServiceMap.containsKey(userName)) {
            audioPlayerService = new AudioPlayerService(config.getAudioOutputStream(meetingId, userName));
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

    private void startVideoPlayer(ImageView localView, String meetingId, String userName) {
        VideoPlayerService videoPlayerService;
        if (!videoPullServiceMap.containsKey(userName)) {
            videoPlayerService = new VideoPlayerService(config.getVideoOutputStream(meetingId, userName),
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
