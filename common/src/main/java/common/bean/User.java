package common.bean;

public class User {
    private String name;

    private String password;

    private String email;

    private String portraitSrc;

    private String host;

    private int port;

    private long timeStamp;

    private boolean registered = true;

    public User() {
    }

    public User(String name, long timeStamp) {
        this.name = name;
        this.timeStamp = timeStamp;
    }

    public User(String name, boolean registered) {
        this.name = name;
        this.registered = registered;
    }

    public User(String name, String password, String email, String src) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.portraitSrc = src;
    }

    public User(String name, String password, String email) {
        this.name = name;
        this.password = password;
        this.email = email;
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getPortraitSrc() {
        return portraitSrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setPortraitSrc(String portraitSrc) {
        this.portraitSrc = portraitSrc;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", portraitSrc='" + portraitSrc + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", timeStamp=" + timeStamp +
                ", registered=" + registered +
                '}';
    }
}
