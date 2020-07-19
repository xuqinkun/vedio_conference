package service;

import common.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.TcpUtil;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static util.NIOUtil.TIMEOUT_SECONDS;

public class UserCleanService implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(UserCleanService.class);

    private MeetingCache meetingCache = MeetingCache.getInstance();

    private String meetingID;

    private boolean stopped;

    public UserCleanService(String meetingID) {
        this.meetingID = meetingID;
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Override
    public void run() {
        while (!stopped) {
            List<User> userList = meetingCache.getUserList(meetingID);
            for (User user : userList) {
                if (System.currentTimeMillis() - user.getTimeStamp() > TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS)) {
                    log.warn("User[{}] connection is timeout, do remove!", user.getName());
                    meetingCache.removeUser(meetingID, user.getName());
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        stopped = true;
    }
}
