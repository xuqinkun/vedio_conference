package service.model;

import common.bean.Meeting;
import common.bean.User;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private volatile User currentUser;

    private volatile Meeting currentMeeting;

    private volatile boolean debugMode;

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
}
