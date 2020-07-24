package common.bean;

public class PermissionContext {
    private String meetingID;

    private String userName;

    private OperationType op;

    public PermissionContext() {
    }

    public PermissionContext(String meetingID, String userName, OperationType op) {
        this.meetingID = meetingID;
        this.userName = userName;
        this.op = op;
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

    public OperationType getOp() {
        return op;
    }
}
