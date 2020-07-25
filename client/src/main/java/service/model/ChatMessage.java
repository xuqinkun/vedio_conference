package service.model;

public class ChatMessage {
    private String date;

    private String userName;

    private String content;

    public ChatMessage() {
    }

    public ChatMessage(String date, String userName, String content) {
        this.date = date;
        this.userName = userName;
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "date='" + date + '\'' +
                ", userName='" + userName + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
