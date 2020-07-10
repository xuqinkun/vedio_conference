package service.model;

import common.bean.User;

public class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private User currentUser;

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
