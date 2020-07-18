package service.model;

import common.bean.Meeting;
import common.bean.User;
import service.schedule.video.GrabberScheduledService;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private volatile User currentUser;

    private volatile Meeting currentMeeting;

    private volatile boolean debugMode;

    private volatile String activeLayout;

    private GrabberScheduledService grabberScheduledService;

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
}
