package controller;

import bean.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        System.out.println("login:" + user.getName());
        return String.format("Hello %s!", user.getName());
    }

}
