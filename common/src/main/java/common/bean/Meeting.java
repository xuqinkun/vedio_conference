package common.bean;

public class Meeting {
    private String uuid;

    private String password;

    private String createTime;

    private String startTime;

    private String endTime;

    private boolean started;

    private boolean ended;

    private String meetingType;

    private String owner;

    public Meeting() {
    }

    public Meeting(String uuid, String password, String meetingType,
                   String createTime, String startTime, boolean started, boolean ended) {
        this.uuid = uuid;
        this.password = password;
        this.createTime = createTime;
        this.startTime = startTime;
        this.started = started;
        this.ended = ended;
        this.meetingType = meetingType;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isEnded() {
        return ended;
    }

    public void setEnded(boolean ended) {
        this.ended = ended;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "uuid='" + uuid + '\'' +
                ", password='" + password + '\'' +
                ", createTime=" + createTime +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", started=" + started +
                ", ended=" + ended +
                ", meetingType='" + meetingType + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }
}
