package bean;

public class User {
    private String name;

    private String password;

    private String email;

    private String portrait;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public User(String name, String password, String email, String src) {
        this.name = name;
        this.password = password;
        this.email = email;
        this.portrait = src;
    }

    public String getPortrait() {
        return portrait;
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

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
