package service;

import common.bean.*;
import dao.MeetingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static common.bean.MessageType.*;
import static common.bean.ResultCode.ERROR;
import static common.bean.ResultCode.OK;

@Component
public class MeetingService {
    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private MeetingCache meetingCache = MeetingCache.getInstance();

    private MeetingDao meetingDao;

    private KafkaService kafkaService;

    private Map<String, MeetingCleanService> cleanServiceMap;

    @Autowired
    public void setKafkaService(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Autowired
    public void setMeetingDao(MeetingDao meetingDao) {
        this.meetingDao = meetingDao;
        cleanServiceMap = new HashMap<>();
    }

    public HttpResult<String> createMeeting(Meeting meeting) {
        if (meeting == null || StringUtils.isEmpty(meeting.getUuid())) {
            log.warn("Meeting is null");
            return new HttpResult<>(ResultCode.ERROR, "Invalid meeting");
        }
        try {
            meetingDao.insert(meeting);
            MeetingCleanService service = new MeetingCleanService(meeting.getUuid(), this);
            new Thread(service).start();
            cleanServiceMap.put(meeting.getUuid(), service);
            return new HttpResult<>(ResultCode.OK, String.format("Create meeting[%s] succeed!", meeting.getUuid()));
        } catch (Exception e) {
            log.error(e.getMessage());
            return new HttpResult<>(ResultCode.ERROR, e.getMessage());
        }
    }

    public Meeting findMeeting(String uuid) {
        if (uuid == null) {
            log.warn("uuid is null");
            return null;
        }
        return meetingDao.find(uuid);
    }

    public HttpResult<String> leaveMeeting(String meetingId, User user) {
        Meeting meeting = findMeeting(meetingId);
        if (meeting == null) {
            return new HttpResult<>(ResultCode.ERROR, String.format("Invalid meeting[%s]", meetingId));
        }
        String userName = user.getName();
        if (meeting.getOwner().equals(userName)) {
            kafkaService.sendMessage(meetingId, new Message(END_MEETING, meetingId));
            endMeeting(meetingId);
            log.warn("Meeting[{}] is ended", meetingId);
            return new HttpResult<>(OK, String.format("Meeting[%s] is ended", meetingId));
        } else {
            kafkaService.sendMessage(meetingId, new Message(USER_LEAVE, JsonUtil.toJsonString(user)));
            meetingCache.removeUser(meetingId, userName);
            log.warn("You leave meeting[{}]", meetingId);
            return new HttpResult<>(OK, String.format("You leave meeting[%s]", meetingId));
        }
    }

    public void endMeeting(String meetingID) {
        meetingCache.removeMeeting(meetingID);
        meetingDao.endMeeting(meetingID);
        MeetingCleanService cleanService = cleanServiceMap.get(meetingID);
        if (cleanService == null) {
            log.error("Can't find meeting[ID={}]", meetingID);
            return;
        }
        cleanService.stop();
    }

    public void removeUser(String meetingID, User user) {
        meetingCache.removeUser(meetingID, user.getName());
        kafkaService.sendMessage(meetingID, new Message(USER_LEAVE, JsonUtil.toJsonString(user)));
    }

    public List<User> getUserList(String meetingID) {
        return meetingCache.getUserList(meetingID);
    }

    public HttpResult<String> joinMeeting(Meeting meeting, User user) {
        String uuid = meeting.getUuid();
        Meeting oldMeeting = findMeeting(uuid);
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
}
