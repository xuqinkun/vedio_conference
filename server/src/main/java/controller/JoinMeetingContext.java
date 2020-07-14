package controller;

import common.bean.Meeting;
import common.bean.User;

public class JoinMeetingContext {
    private User user;

    private Meeting meeting;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Meeting getMeeting() {
        return meeting;
    }

    public void setMeeting(Meeting meeting) {
        this.meeting = meeting;
    }
}
