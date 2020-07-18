package common.bean;

public class User {
    private String name;

    private String password;

    private String email;

    private String portraitSrc;

    private boolean registered = true;

    public User() {
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

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", portrait='" + portraitSrc + '\'' +
                ", registered=" + registered +
                '}';
    }
}
