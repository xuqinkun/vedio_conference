package service;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import common.bean.User;
import dao.MeetingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MeetingService {
    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private MeetingCache meetingCache = MeetingCache.getInstance();

    private MeetingDao meetingDao;

    private Map<String, MeetingCleanService> cleanServiceMap;

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

    public void leaveMeeting(String meetingId) {
        cleanServiceMap.get(meetingId).stop();
    }

    public void endMeeting(String meetingID) {
        meetingCache.removeMeeting(meetingID);
        meetingDao.endMeeting(meetingID);
    }

    public void removeUser(String meetingID, User user) {
        meetingCache.removeUser(meetingID, user.getName());
    }

    public List<User> getUserList(String meetingID) {
        return meetingCache.getUserList(meetingID);
    }
}
