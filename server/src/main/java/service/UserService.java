package service;

import common.bean.HttpResult;
import common.bean.User;
import dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static common.bean.ResultCode.ERROR;
import static common.bean.ResultCode.OK;

@Component
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void register(User user) {
        if (user == null) {
            log.warn("User is null");
            return;
        }
        userDao.insert(user);
        log.debug("Insert user[{}] succeed!", user);
    }

    public HttpResult<String> login(User user) {
        if (user == null) {
            log.warn("User is null");
            return new HttpResult<>(ERROR, "User is null!");
        }
        User findUser = userDao.findOne(user.getName());
        if (findUser == null) {
            return new HttpResult<>(ERROR, "Cannot find user[" + user.getName() + "]");
        }
        if (!findUser.getPassword().equals(user.getPassword())) {
            return new HttpResult<>(ERROR, String.format("Password for %s is incorrect!", user.getName()));
        }
        return new HttpResult<>(OK, user.getName() + " login succeed!");
    }

    public User findOne(String username) {
        if (username == null) {
            return null;
        }
        return userDao.findOne(username);
    }
}
