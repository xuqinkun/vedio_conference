package service;

import common.bean.HttpResult;
import common.bean.Meeting;
import common.bean.ResultCode;
import dao.MeetingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MeetingService {
    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private MeetingDao meetingDao;

    @Autowired
    public void setMeetingDao(MeetingDao meetingDao) {
        this.meetingDao = meetingDao;
    }

    public HttpResult<String> createMeeting(Meeting meeting) {
        if (meeting == null || StringUtils.isEmpty(meeting.getUuid())) {
            log.warn("Meeting is null");
            return new HttpResult<>(ResultCode.ERROR, "Invalid meeting");
        }
        try {
            meetingDao.insert(meeting);
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
}