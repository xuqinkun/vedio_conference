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

import static common.bean.OperationType.*;
import static common.bean.ResultCode.ERROR;

@RestController
public class MeetingController {

    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private MeetingService meetingService;

    private UserService userService;

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
        if (meeting == null || StringUtils.isEmpty(meeting.getHost())) {
            return new HttpResult<>(ERROR, "Invalid meeting");
        }
        User user = userService.findOne(meeting.getHost());
        if (user == null) {
            return new HttpResult<>(ERROR, "Invalid owner[" + meeting.getHost() + "]");
        }
        HttpResult<String> ret = meetingService.createMeeting(meeting, user);
        log.warn("{} created meeting[ID={}]", user.getName(), meeting.getUuid());
        updateUserInfo(request, user);
        return ret;
    }

    @PostMapping("/getUserList")
    public @ResponseBody
    HttpResult<String> getUserList(@RequestBody String uuid) {
        return meetingService.getUserList(uuid);
    }

    @RequestMapping(value = "/joinMeeting")
    public @ResponseBody
    HttpResult<String> joinMeeting(@RequestBody MeetingContext context, HttpServletRequest request) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();

        updateUserInfo(request, user);

        log.warn("User[{}] join meeting[ID={}] host: {} port: {}", meeting.getUuid(),
                user.getName(), user.getHost(), user.getPort());
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
        user.setHost(remoteHost);
        user.setPort(remotePort);
        user.setTimeStamp(System.currentTimeMillis());
    }

    @PostMapping("/permissionControl")
    public @ResponseBody
    HttpResult<String> permissionControl(@RequestBody PermissionContext context) {
        String meetingID = context.getMeetingID();
        String userName = context.getUserName();
        OperationType op = context.getOp();

        if (StringUtils.isEmpty(meetingID) || StringUtils.isEmpty(userName)) {
            log.warn("MeetingId or username is null");
            return new HttpResult<>(ERROR, "MeetingId or username is null");
        }
        if (op == HOST_CHANGE) {
            return meetingService.changeHost(meetingID, userName);
        } else if (op == MANAGER_ADD) {
            return meetingService.addManger(meetingID, userName);
        } else if (op == MANAGER_REMOVE) {
            return meetingService.removeManager(meetingID, userName);
        } else if (op == VIDEO_ON) {
            return meetingService.openVideoPermission(meetingID, userName);
        } else if (op == VIDEO_OFF) {
            return meetingService.forbidVideoPermission(meetingID, userName);
        } else if (op == AUDIO_ON) {
            return meetingService.openAudioPermission(meetingID, userName);
        } else if (op == AUDIO_OFF) {
            return meetingService.forbidAudioPermission(meetingID, userName);
        }
        return new HttpResult<>(ERROR, "Operation is not supported!");
    }
}
