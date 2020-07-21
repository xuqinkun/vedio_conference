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

import static common.bean.ResultCode.ERROR;

@RestController
public class MeetingController {

    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private MeetingService meetingService;

    private UserService userService;

    private MeetingCache meetingCache = MeetingCache.getInstance();

    @Autowired
    public void setMeetingService(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
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
        log.warn("{} created meeting[ID={}]", user.getName(), meeting.getUuid());
        // Must add user first
        meetingCache.addUser(meeting.getUuid(), user);
        HttpResult<String> ret = meetingService.createMeeting(meeting);
        updateUserInfo(request, user);
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
    HttpResult<String> joinMeeting(@RequestBody MeetingContext context, HttpServletRequest request) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();

        updateUserInfo(request, user);
        log.warn("User[{}] join meeting[{}]", user.getName(), meeting.getUuid());
        return meetingService.joinMeeting(meeting, user);
    }

    @RequestMapping(value = "/leaveMeeting")
    public @ResponseBody
    HttpResult<String> leaveMeeting(@RequestBody MeetingContext context, HttpServletRequest request) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();
        log.warn("User[{}] leave meeting[{}]", user.getName(), meeting.getUuid());
        return meetingService.leaveMeeting(meeting.getUuid(), user);
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
