package service.model;

public class User {
    private String username;

    private String password;

    private String email;

    private String portrait;

    public User(String username, String password, String email, String portrait) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.portrait = portrait;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPortrait() {
        return portrait;
    }
}
