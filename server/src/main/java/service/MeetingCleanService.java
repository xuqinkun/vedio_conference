package service;

import common.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;
import util.Helper;

import java.util.List;
import java.util.concurrent.TimeUnit;


public class MeetingCleanService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MeetingCleanService.class);

    private String meetingID;

    private boolean stopped;

    private MeetingService meetingService;

    private static final int TIMEOUT_SECONDS = Config.getInstance().getHeartBeatsTimeout();

    public MeetingCleanService(String meetingID, MeetingService meetingService) {
        this.meetingID = meetingID;
        this.meetingService = meetingService;
    }

    @Override
    public void run() {
        while (!stopped) {
            List<User> userList = MeetingCache.getInstance().getUserList(meetingID);
            if(userList == null) {
                log.warn("Meeting[{}] ended. Stop cleaning.", meetingID);
                return;
            }
            else if (userList.size() == 0) {
                log.warn("No user in the meeting[{}], remove it.", meetingID);
                meetingService.endMeeting(meetingID);
            } else {
                for (User user : userList) {
                    if (System.currentTimeMillis() - user.getTimeStamp() > TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
                        log.warn("User[{}] connection is timeout, last alive time[{}], do remove!",
                                user.getName(), Helper.dateFormat(user.getTimeStamp()));
                        meetingService.removeUser(meetingID, user);
                    }
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
