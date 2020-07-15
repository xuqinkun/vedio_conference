package controller;

import common.bean.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import service.KafkaService;
import service.MeetingService;
import service.UserService;
import util.JsonUtil;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.bean.MessageType.USER_ADD;
import static common.bean.ResultCode.ERROR;

@RestController
public class MeetingController {

    private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

    private MeetingService meetingService;

    private UserService userService;

    private KafkaService kafkaService;

    private Map<String, List<User>> meetingUserMap = new HashMap<>();

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
    HttpResult<String> getUserList(@RequestBody String uuid) {
        if (uuid == null || meetingUserMap.get(uuid) == null) {
            return new HttpResult<>(ERROR, "[]");
        }
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(meetingUserMap.get(uuid)));
    }

    @PostMapping(value = "/joinMeeting", produces = "application/json;charset=UTF-8")
    public @ResponseBody
    HttpResult<String> getUserList(@RequestBody JoinMeetingContext context) {
        Meeting meeting = context.getMeeting();
        User user = context.getUser();
        String uuid = meeting.getUuid();
        Meeting oldMeeting = meetingService.findMeeting(uuid);
        if (oldMeeting == null || !oldMeeting.getPassword().equals(meeting.getPassword())) {
            String errMessage = String.format("Can't find meeting[uuid=%s] or meeting password is wrong\n", uuid);
            log.error(errMessage);
            return new HttpResult<>(ERROR, errMessage);
        }
        kafkaService.sendMessage(uuid, new Message(USER_ADD, JsonUtil.toJsonString(user)));
        meetingUserMap.get(uuid).add(user);
//        Map<String, String> resp = new HashMap<>();
//        resp.put("meeting", JsonUtil.toJsonString(oldMeeting));
//        resp.put("users", JsonUtil.toJsonString(meetingUserMap.get(uuid)));
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(oldMeeting));
    }
}
