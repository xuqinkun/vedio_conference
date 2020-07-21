package service.model;

import common.bean.Meeting;
import common.bean.User;
import service.schedule.video.GrabberScheduledService;
import util.Config;
import util.DeviceManager;

import java.util.HashMap;
import java.util.Map;

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

    public String getPortraitSrc(String username) {
        User user = userCache.get(username);
        return user == null || user.getPortraitSrc() == null ? config.getDefaultPortraitSrc() : user.getPortraitSrc();
    }

    public boolean isMeetingOwner() {
        return currentMeeting.getOwner().equals(currentUser.getName());
    }

    public void stopMeeting() {
        DeviceManager.stopDevices(currentMeeting.getUuid());
        currentMeeting = null;
    }
}
