package common.bean;

public class MeetingContext {
    private User user;

    private Meeting meeting;

    public MeetingContext() {
    }

    public MeetingContext(User user, Meeting meeting) {
        this.user = user;
        this.meeting = meeting;
    }

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
