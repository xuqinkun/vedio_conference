package service.model;

import common.bean.Meeting;
import common.bean.User;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;

    private Meeting currentMeeting;

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
        return currentUser;
    }


}
