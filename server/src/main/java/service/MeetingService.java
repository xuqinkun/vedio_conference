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
import java.util.Map;
import java.util.Set;

import static common.bean.OperationType.*;
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

    public HttpResult<String> createMeeting(Meeting meeting, User user) {
        if (meeting == null || StringUtils.isEmpty(meeting.getUuid())) {
            log.warn("Meeting is null");
            return new HttpResult<>(ResultCode.ERROR, "Invalid meeting");
        }
        try {
            // Must add user first
            meetingCache.addUser(meeting.getUuid(), user);
            // Add creator to managers
            meeting.getManagers().add(user.getName());
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

    public HttpResult<String> leaveMeeting(String meetingID, User user) {
        Meeting meeting = findMeeting(meetingID);
        if (meeting == null) {
            return new HttpResult<>(ResultCode.ERROR, String.format("Invalid meeting[%s]", meetingID));
        }
        String userName = user.getName();
        if (meeting.getHost().equals(userName)) {
            kafkaService.sendMessage(meetingID, new Message(END_MEETING, meetingID));
            endMeeting(meetingID);
            log.warn("Meeting[{}] is ended", meetingID);
            return new HttpResult<>(OK, String.format("Meeting[%s] is ended", meetingID));
        } else {
            kafkaService.sendMessage(meetingID, new Message(USER_REMOVE, JsonUtil.toJsonString(user)));
            meetingCache.removeUser(meetingID, userName);
            return new HttpResult<>(OK, String.format("You leave meeting[%s]", meetingID));
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
        kafkaService.sendMessage(meetingID, new Message(USER_REMOVE, JsonUtil.toJsonString(user)));
    }

    public HttpResult<String> getUserList(String uuid) {
        if (uuid == null || meetingCache.getUserList(uuid) == null) {
            return new HttpResult<>(ERROR, "[]");
        }
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(meetingCache.getUserList(uuid)));
    }

    public HttpResult<String> joinMeeting(Meeting meeting, User user) {
        String uuid = meeting.getUuid();
        Meeting oldMeeting = findMeeting(uuid);
        if (oldMeeting == null || !oldMeeting.getPassword().equals(meeting.getPassword())) {
            String errMessage = String.format("Can't find meeting[ID=%s] or password is not correct.\n", uuid);
            log.error(errMessage);
            return new HttpResult<>(ERROR, errMessage);
        }
        if (oldMeeting.isEnded()) {
            return new HttpResult<>(ERROR, String.format("Sorry, meeting[ID=%s] is ended.", uuid));
        }
        kafkaService.sendMessage(uuid, new Message(USER_ADD, JsonUtil.toJsonString(user)));
        User cacheUser = meetingCache.getUser(uuid, user.getName());
        if (cacheUser == null) {
            meetingCache.addUser(uuid, user);
        }
        return new HttpResult<>(ResultCode.OK, JsonUtil.toJsonString(oldMeeting));
    }

    public HttpResult<String> changeHost(String meetingID, String userName) {
        Meeting oldMeeting = findMeeting(meetingID);
        if (oldMeeting == null) {
            String errMsg = String.format("Can't find meeting[ID=%s]", meetingID);
            log.error(errMsg);
            return new HttpResult<>(ERROR, errMsg);
        }
        String oldHost = oldMeeting.getHost();
        oldMeeting.getManagers().remove(oldHost);
        oldMeeting.setHost(userName);
        meetingDao.updateHost(meetingID, oldMeeting);
        kafkaService.sendMessage(meetingID, new Message(HOST_CHANGE, userName));
        return new HttpResult<>(OK, String.format("Meeting[ID=%s] host is changed to %s", meetingID, userName));
    }

    public HttpResult<String> addManger(String meetingID, String userName) {
        Meeting oldMeeting = findMeeting(meetingID);
        if (oldMeeting == null) {
            String errMsg = String.format("Can't find meeting[ID=%s]", meetingID);
            log.error(errMsg);
            return new HttpResult<>(ERROR, errMsg);
        }
        Set<String> managers = oldMeeting.getManagers();
        if (managers.contains(userName)) {
            return new HttpResult<>(ERROR, String.format("User[%s] is manager already!", userName));
        }
        managers.add(userName);
        meetingDao.updateManagers(meetingID, managers);
        kafkaService.sendMessage(meetingID, new Message(MANAGER_ADD, userName));
        return new HttpResult<>(OK, String.format("User[%s] is manager now!", userName));
    }

    public HttpResult<String> removeManager(String meetingID, String userName) {
        Meeting oldMeeting = findMeeting(meetingID);
        if (oldMeeting == null) {
            String errMsg = String.format("Can't find meeting[ID=%s]", meetingID);
            log.error(errMsg);
            return new HttpResult<>(ERROR, errMsg);
        }
        Set<String> managers = oldMeeting.getManagers();
        managers.remove(userName);
        meetingDao.updateManagers(meetingID, managers);
        kafkaService.sendMessage(meetingID, new Message(MANAGER_REMOVE, userName));
        return new HttpResult<>(OK, String.format("User[%s] is removed from manager members!", userName));
    }

    public HttpResult<String> openVideoPermission(String userName) {
        kafkaService.sendMessage(userName, new Message(VIDEO_ON, userName));
        return new HttpResult<>(OK, String.format("User[%s] is allowed to open video", userName));
    }

    public HttpResult<String> forbidVideoPermission(String userName) {
        kafkaService.sendMessage(userName, new Message(VIDEO_OFF, userName));
        return new HttpResult<>(OK, String.format("User[%s] is forbidden to open video", userName));
    }

    public HttpResult<String> openAudioPermission(String userName) {
        kafkaService.sendMessage(userName, new Message(AUDIO_ON, userName));
        return new HttpResult<>(OK, String.format("User[%s] is allowed to open audio", userName));
    }

    public HttpResult<String> forbidAudioPermission(String userName) {
        kafkaService.sendMessage(userName, new Message(AUDIO_OFF, userName));
        return new HttpResult<>(OK, String.format("User[%s] is forbidden to open audio", userName));
    }
}
