package service.schedule.layout;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.OperationType;
import common.bean.User;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.http.HttpClientUtil;
import service.http.UrlMap;
import service.messaging.MessageReceiveService;
import service.model.SessionManager;
import service.schedule.TaskStarter;
import service.schedule.audio.AudioPlayerService;
import service.schedule.video.GrabberScheduledService;
import service.schedule.video.VideoPlayerService;
import service.schedule.video.VideoRecordTask;
import util.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static common.bean.OperationType.*;

public class MeetingRoomControlTask extends Task<LayoutChangeSignal> {

    private static final Logger log = LoggerFactory.getLogger(TaskStarter.class);
    public static final Config config = Config.getInstance();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ScheduledThreadPoolExecutor exec = ThreadPoolUtil.getScheduledExecutor(5, "MeetingRoomStart");

    private final Map<String, VideoPlayerService> videoPullServiceMap = new HashMap<>();
    private final Map<String, AudioPlayerService> audioPlayerServiceMap = new HashMap<>();

    private Label hostLabel;
    private ImageView globalView;
    private StackPane lastClicked;
    private VideoRecordTask videoRecordTask;

    public MeetingRoomControlTask(Label hostLabel, Pane userListLayout, ImageView globalView) {
        this.hostLabel = hostLabel;
        this.globalView = globalView;
        initListener(userListLayout);
    }

    private void initListener(Pane userListLayout) {
        valueProperty().addListener((observable, oldValue, layoutChangeSignal) -> {
            if (layoutChangeSignal != null) {
                Parent root = userListLayout.getScene().getRoot();
                ImageView globalImageView = (ImageView) root.lookup("#globalImageView");

                OperationType type = layoutChangeSignal.getOp();
                String controlID = layoutChangeSignal.getControlID();
                if (type == USER_ADD) {
                    log.warn("USER_ADD[{}]", controlID);
                    userListLayout.getChildren().add(layoutChangeSignal.getPane());
                } else if (type == USER_REMOVE) {
                    log.warn("USER_LEAVE[{}]", controlID);
                    Node node = userListLayout.lookup("#" + controlID);
                    userListLayout.getChildren().remove(node);
                } else if (type == VIDEO_OFF) {
                    Label videoBtnLabel = (Label) root.lookup("#videoBtnLabel");
                    if (videoBtnLabel.getText().contains("On")) {
                        VBox videoSwitchBtn = (VBox) root.lookup("#videoSwitchBtn");
                        new VideoSwitchTask(videoSwitchBtn, globalImageView);
                    }
                } else if (type == AUDIO_OFF) {
                    Label videoBtnLabel = (Label) root.lookup("#audioBtnLabel");
                    // Close audio call if it is opening
                    if (videoBtnLabel.getText().contains("On")) {
                        VBox audioSwitchBtn = (VBox) root.lookup("#audioSwitchBtn");
                        new AudioSwitchTask(audioSwitchBtn);
                    }
                }
            }
        });
        exceptionProperty().addListener((observable, oldValue, newValue) -> {
            log.error(newValue.getMessage());
        });
    }

    private void init() {
        String username = sessionManager.getCurrentUser().getName();
        Meeting currentMeeting = sessionManager.getCurrentMeeting();

        initializeDevice();
        sessionManager.setActiveLayout(username);
        /* Display user list */
        initUserList(currentMeeting);
        /* Listen user change (join or leave)*/
        messageListener(currentMeeting, username);
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
        if (currentMeeting.getHost().equals(currentUser.getName())) { // Is meeting owner
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

    private MessageReceiveService globalReceiveService;

    private MessageReceiveService personalReceiveTask;

    private void messageListener(Meeting currentMeeting, String userName) {
        globalReceiveService = new MessageReceiveService(currentMeeting.getUuid(), hostLabel, userName + "_global", this);
        personalReceiveTask = new MessageReceiveService(userName, hostLabel, userName + "_local", this);
        globalReceiveService.start();
        personalReceiveTask.start();
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
        stackPane.setAlignment(Pos.BOTTOM_CENTER);

        if (sessionManager.isCurrentUser(userName)) {
            stackPane.setStyle(activeStyle);
            lastClicked = stackPane;
        } else {
            stackPane.setStyle(normalStyle);
        }

        String portrait = user.getPortraitSrc() == null ? config.getDefaultPortraitSrc() : user.getPortraitSrc();
        Image image = new Image(portrait);
        ImageView userView = new ImageView(image);
        userView.setFitWidth(stackPane.getPrefWidth() - 7);
        userView.setFitHeight(stackPane.getPrefHeight() - 10);

        Label label = new Label(user.getName());
        label.setStyle("-fx-text-fill: white;-fx-background-color: #000000;-fx-font-size: 14");
        label.setPrefSize(userView.getFitWidth() - 25, 25);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setOpacity(0.3);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BOTTOM_CENTER);
        hBox.getChildren().add(label);

        String meetingID = sessionManager.getCurrentMeeting().getUuid();
        MenuBar menuBar = createMenuBar(userName, meetingID);

        hBox.getChildren().add(menuBar);
        stackPane.getChildren().add(userView);
        stackPane.getChildren().add(hBox);

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

        updateValue(new LayoutChangeSignal(USER_ADD, userName, stackPane));
        try {
            // Waiting for layout loading
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.warn("User[{}] added", user);
        if (!sessionManager.isCurrentUser(userName)) {
            exec.submit(() -> {
                startVideoPlayer(userView, meetingID, userName);
                startAudioPlayer(meetingID, userName);
            });
        } else if (sessionManager.isDebugMode()) {
            startAudioPlayer(meetingID, userName);
        }
        if (sessionManager.isCurrentUser(userName)) {
            if (sessionManager.getGrabberScheduledService() == null) {
                GrabberScheduledService grabberScheduledService = new GrabberScheduledService(userView, globalView, userName);
                sessionManager.setGrabberScheduledService(grabberScheduledService);
            } else {
                GrabberScheduledService service = sessionManager.getGrabberScheduledService();
                service.setLocalView(userView);
                service.setGlobalView(globalView);
                service.setLayoutName(userName);
            }
        }
    }

    private MenuBar createMenuBar(String userName, String meetingID) {
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menuBar_" + userName);
        menuBar.setStyle("-fx-background-color: white;-fx-pref-width: 20;-fx-pref-height: 20;-fx-opacity: 0.4");

        Menu menu = new Menu();
        ImageView img = new ImageView(new Image("/fxml/img/menu.png"));
        img.setFitHeight(20);
        img.setFitWidth(20);
        menu.setGraphic(img);

        MenuItem host = new MenuItem("Appoint as host");
        MenuItem manager = new MenuItem("Appoint as manager");
        host.setOnAction(event -> {
            if (!sessionManager.isMeetingHost()) {
                SystemUtil.showSystemInfo("You are not host. Operation not supported!");
                return;
            }
            if (sessionManager.isMeetingHost(userName)) {
                SystemUtil.showSystemInfo("You are host already!");
                return;
            }
            new PermissionService(meetingID, userName, HOST_CHANGE).start();
            event.consume();
        });
        manager.setOnAction(event -> {
            if (!sessionManager.isMeetingHost()) {
                SystemUtil.showSystemInfo("You are not host. Operation not supported!");
                return;
            }
            if (sessionManager.isMeetingManager(userName)) {
                SystemUtil.showSystemInfo(String.format("%s is manager already!", userName));
                return;
            }
            new PermissionService(meetingID, userName, MANAGER_ADD).start();
        });
        menu.getItems().addAll(host, manager);

        MenuItem audioSwitch = new MenuItem("Audio on");
        MenuItem videoSwitch = new MenuItem("Video on");

        audioSwitch.setOnAction(event -> {
            if (audioSwitch.getText().contains("on")) {
                audioSwitch.setText("Audio off");
                new PermissionService(meetingID, userName, AUDIO_ON).start();
            } else {
                audioSwitch.setText("Audio on");
                new PermissionService(meetingID, userName, AUDIO_OFF).start();
            }
        });
        videoSwitch.setOnAction(event -> {
            if (audioSwitch.getText().contains("on")) {
                audioSwitch.setText("Video off");
                new PermissionService(meetingID, userName, VIDEO_ON).start();
            } else {
                audioSwitch.setText("Video on");
                new PermissionService(meetingID, userName, VIDEO_OFF).start();
            }
        });

        menu.getItems().addAll(audioSwitch, videoSwitch);
        menuBar.getMenus().add(menu);
        return menuBar;
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

    public void stopMeeting() {
        for (VideoPlayerService service : videoPullServiceMap.values()) {
            service.cancel();
        }
        for (AudioPlayerService service : audioPlayerServiceMap.values()) {
            service.cancel();
        }
        globalReceiveService.cancel();
        personalReceiveTask.cancel();
        exec.remove(videoRecordTask);
        exec.shutdownNow();
    }

    @Override
    protected LayoutChangeSignal call() {
        init();
        return null;
    }

    public void addSignal(LayoutChangeSignal layoutChangeSignal) {
        updateValue(layoutChangeSignal);
    }
}
