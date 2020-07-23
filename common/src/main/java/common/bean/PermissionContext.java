package common.bean;

public class PermissionContext {
    private String meetingID;

    private String userName;

    private String operation;

    public PermissionContext() {
    }

    public PermissionContext(String meetingID, String userName) {
        this.meetingID = meetingID;
        this.userName = userName;
    }

    public String getMeetingID() {
        return meetingID;
    }

    public void setMeetingID(String meetingID) {
        this.meetingID = meetingID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
