package controller;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import common.bean.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import service.MeetingService;
import service.UserService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.bean.ResultCode.ERROR;

@RestController
public class MeetingController {
    private MeetingService meetingService;

    private UserService userService;

    private Map<String, List<User>> meetingUserMap = new HashMap<>();

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
    HttpResult<String> createMeeting(@RequestBody Meeting meeting, HttpSession session) {
        if (meeting == null || StringUtils.isEmpty(meeting.getOwner())) {
            return new HttpResult<>(ERROR, "Invalid meeting");
        }
        User user = userService.findOne(meeting.getOwner());
        if (user == null) {
            return new HttpResult<>(ERROR, "Invalid owner[" + meeting.getOwner() + "]");
        }
        HttpResult<String> ret = meetingService.createMeeting(meeting);
        meetingUserMap.put(meeting.getUuid(), new ArrayList<>());
        meetingUserMap.get(meeting.getUuid()).add(user);
        return ret;
    }

    @PostMapping("/getUserList")
    public @ResponseBody
    HttpResult<List<User>> getUserList(@RequestBody String uuid) {
        if (uuid == null || meetingUserMap.get(uuid) == null) {
            return new HttpResult<>(ERROR, new ArrayList<>());
        }
        return new HttpResult<>(ResultCode.OK, meetingUserMap.get(uuid));
    }

    @PostMapping(value = "/joinMeeting", produces = "application/json;charset=UTF-8")
    public @ResponseBody
    HttpResult<List<User>> getUserList(@RequestBody JoinMeetingContext context) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();
        String uuid = meeting.getUuid();
        Meeting oldMeeting = meetingService.findMeeting(uuid);
        if (oldMeeting == null || !oldMeeting.getPassword().equals(meeting.getPassword())) {
            return new HttpResult<>(ERROR, null);
        }
        meetingUserMap.get(uuid).add(user);
        return new HttpResult<>(ResultCode.OK, meetingUserMap.get(uuid));
    }
}
