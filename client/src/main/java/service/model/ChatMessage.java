package service.model;

public class ChatMessage {
    private String date;

    private String senderName;

    private String receiver;

    private String content;

    private boolean isPersonal;

    public ChatMessage() {
    }

    public ChatMessage(String date, String senderName, String content) {
        this.date = date;
        this.senderName = senderName;
        this.content = content;
    }

    public boolean isPersonal() {
        return isPersonal;
    }

    public void setPersonal(boolean personal) {
        isPersonal = personal;
    }

    public String getDate() {
        return date;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "date='" + date + '\'' +
                ", senderName='" + senderName + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content='" + content + '\'' +
                ", isPersonal=" + isPersonal +
                '}';
    }
}
