package service.model;

import common.bean.Meeting;
import common.bean.OperationType;
import common.bean.User;
import service.schedule.layout.ManagerLayoutRefreshService;
import service.schedule.video.GrabberScheduledService;
import util.Config;
import util.DeviceManager;
import util.SystemUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static common.bean.MeetingType.*;
import static common.bean.OperationType.AUDIO_ON;
import static common.bean.OperationType.VIDEO_ON;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();
    public static final Config config = Config.getInstance();

    private volatile User currentUser;

    private volatile Meeting currentMeeting;

    private volatile boolean debugMode;

    private volatile String activeLayout;

    private GrabberScheduledService grabberScheduledService;

    private Map<String, User> userCache = new HashMap<>();

    public Meeting getCurrentMeeting() {
        return currentMeeting;
    }

    public void setCurrentMeeting(Meeting currentMeeting) {
        this.currentMeeting = currentMeeting;
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        if (currentUser == null) {
            currentUser = createDefault();
        }
        return currentUser;
    }

    public boolean isCurrentUser(String username) {
        return username.equals(currentUser.getName());
    }

    public void addUser(User user) {
        userCache.put(user.getName(), user);
    }

    public void removeUser(User user) {
        userCache.remove(user.getName());
    }

    public User getUser(String username) {
        return userCache.get(username);
    }

    public Set<String> getUserList() {
        return userCache.keySet();
    }

    private User createDefault() {
        return new User("user_" + System.currentTimeMillis(), false);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void setActiveLayout(String layoutName) {
        activeLayout = layoutName;
    }

    public String getActiveLayout() {
        return activeLayout;
    }

    public void setGrabberScheduledService(GrabberScheduledService grabberScheduledService) {
        this.grabberScheduledService = grabberScheduledService;
    }

    public GrabberScheduledService getGrabberScheduledService() {
        return grabberScheduledService;
    }

    public boolean isMeetingHost() {
        return currentMeeting.getHost().equals(currentUser.getName());
    }

    public void stopMeeting() {
        DeviceManager.stopDevices(currentMeeting.getUuid());
        currentMeeting = null;
    }

    public boolean hasPermission(OperationType operation, boolean showInfo) {
        String meetingType = currentMeeting.getMeetingType();
        if (meetingType == null) { // For debug
            return true;
        } else if (isMeetingHost()) {
            return true;
        }
        else if (meetingType.equals(PERSONAL.getValue())) {
            return true;
        } else if (meetingType.equals(BUSINESS.getValue())) {
            if (isMeetingManager()) {
                return true;
            } else if (operation == AUDIO_ON && isAudioCallAllowed()) {
                return true;
            } else if (operation == VIDEO_ON && isVideoCallAllowed()) {
                return true;
            } else {
                if (showInfo)
                    SystemUtil.showSystemInfo(String.format("Sorry! You don't have the %s permission. " +
                            "Please ask managers to assign the permission.", operation.toString().toLowerCase()));
                return false;
            }
        } else if (meetingType.equals(SPEECH.getValue())) {
            if (showInfo)
                SystemUtil.showSystemInfo(String.format("Sorry! Current meeting type is speech, host have the %s permission only.",
                        operation.toString().toLowerCase()));
            return false;
        }
        return false;
    }

    public Set<String> getManagers() {
        return currentMeeting.getManagers();
    }

    public boolean isMeetingManager() {
        String userName = currentUser.getName();
        return currentMeeting.getHost().equals(userName) || getManagers().contains(userName);
    }

    public boolean isMeetingHost(String userName) {
        return userName.equals(currentMeeting.getHost());
    }

    public boolean isMeetingManager(String userName) {
        return getManagers().contains(userName);
    }

    private volatile ManagerLayoutRefreshService refreshService;

    public void setManagerLayoutRefreshService(ManagerLayoutRefreshService refreshService) {
        this.refreshService = refreshService;
    }

    public ManagerLayoutRefreshService getRefreshService() {
        return refreshService;
    }

    private boolean videoCallAllowed = false;
    private boolean audioCallAllowed = false;

    public void setVideoCallAllowed(boolean videoCallAllowed) {
        this.videoCallAllowed = videoCallAllowed;
    }

    public void setAudioCallAllowed(boolean audioCallAllowed) {
        this.audioCallAllowed = audioCallAllowed;
    }

    public boolean isVideoCallAllowed() {
        return videoCallAllowed;
    }

    public boolean isAudioCallAllowed() {
        return audioCallAllowed;
    }
}
