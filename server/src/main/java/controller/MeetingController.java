package controller;

import common.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import service.*;
import util.JsonUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static common.bean.MessageType.USER_ADD;
import static common.bean.ResultCode.ERROR;

@RestController
public class MeetingController {

    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private MeetingService meetingService;

    private UserService userService;

    private KafkaService kafkaService;

    private MeetingCache meetingCache = MeetingCache.getInstance();

    @Autowired
    public void setMeetingService(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @Autowired
    public void setKafkaService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/test")
    public void test() {
        kafkaService.sendMessage("test", new Message(USER_ADD, new User()));
    }

    @PostMapping("/createMeeting")
    public @ResponseBody
    HttpResult<String> createMeeting(@RequestBody Meeting meeting, HttpServletRequest request) {
        if (meeting == null || StringUtils.isEmpty(meeting.getOwner())) {
            return new HttpResult<>(ERROR, "Invalid meeting");
        }
        User user = userService.findOne(meeting.getOwner());
        if (user == null) {
            return new HttpResult<>(ERROR, "Invalid owner[" + meeting.getOwner() + "]");
        }
        HttpResult<String> ret = meetingService.createMeeting(meeting);
        updateUserInfo(request, user);
        meetingCache.addUser(meeting.getUuid(), user);
        return ret;
    }

    @PostMapping("/getUserList")
    public @ResponseBody
    HttpResult<String> getUserList(@RequestBody String uuid) {
        if (uuid == null || meetingCache.getUserList(uuid) == null) {
            return new HttpResult<>(ERROR, "[]");
        }
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(meetingCache.getUserList(uuid)));
    }

    @RequestMapping(value = "/joinMeeting")
    public @ResponseBody
    HttpResult<String> joinMeeting(@RequestBody JoinMeetingContext context, HttpServletRequest request) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();

        updateUserInfo(request, user);

        String uuid = meeting.getUuid();
        Meeting oldMeeting = meetingService.findMeeting(uuid);
        if (oldMeeting == null || !oldMeeting.getPassword().equals(meeting.getPassword())) {
            String errMessage = String.format("Can't find meeting[ID=%s] or password is not correct.\n", uuid);
            log.error(errMessage);
            return new HttpResult<>(ERROR, errMessage);
        }
        kafkaService.sendMessage(uuid, new Message(USER_ADD, JsonUtil.toJsonString(user)));
        User cacheUser = meetingCache.getUser(uuid, user.getName());
        if (cacheUser == null) {
            meetingCache.addUser(uuid, user);
        }
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(oldMeeting));
    }

    private void updateUserInfo(HttpServletRequest request, User user) {
        String remoteHost = request.getRemoteHost();
        int remotePort = request.getRemotePort();
        String userName = user.getName();

        user.setHost(remoteHost);
        user.setPort(remotePort);
        user.setTimeStamp(System.currentTimeMillis());

        log.warn("User[{}] join meeting host: {} port: {}", userName, remoteHost, remotePort);
    }
}
