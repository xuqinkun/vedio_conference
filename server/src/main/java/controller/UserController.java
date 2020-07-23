package controller;

import common.bean.HttpResult;
import common.bean.ResultCode;
import common.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.UserService;

import javax.servlet.http.HttpSession;

import java.util.Date;

import static common.bean.ResultCode.ERROR;
import static common.bean.ResultCode.OK;

@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public @ResponseBody
    ResponseEntity<HttpResult<String>> login(@RequestBody User user, HttpSession session) {
        log.debug("login:[{}]", user.getName());
        try {
            if (user.getName() == null) {
                log.warn("User name is null!");
                return new ResponseEntity<>(new HttpResult<>(ERROR, "User name is null"), HttpStatus.UNAUTHORIZED);
            }
            if (session.getAttribute(user.getName()) != null) {
                log.debug("User[{}] has login before!", user.getName());
                return new ResponseEntity<>(new HttpResult<>(ERROR, user.getName() + " has login before!"), HttpStatus.OK);
            }
            HttpResult<String> result = userService.login(user);
            if (result.getResult() == OK) {
                session.setAttribute(user.getName(), user);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(result, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("{}", e.toString());
            return new ResponseEntity<>(new HttpResult<>(ERROR, e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public @ResponseBody
    ResponseEntity<HttpResult<String>> register(@RequestBody User user) {
        log.debug("login:[{}]", user.getName());
        if (user.getName() == null) {
            log.warn("User name is null!");
            return new ResponseEntity<>(new HttpResult<>(ERROR, "User name is null"), HttpStatus.OK);
        }
        HttpResult<String> res = new HttpResult<>();
        try {
            if (userService.findOne(user.getName()) != null) {
                HttpResult<String> result = new HttpResult<>(ERROR, String.format("Username[%s] is registered!", user.getName()));
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            userService.register(user);
            res.setResult(ResultCode.OK);
            res.setMessage(String.format("User[%s] registered succeed!", user.getName()));
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("{}", e.toString());
            res.setResult(ERROR);
            res.setMessage(e.getMessage());
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
