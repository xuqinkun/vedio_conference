package util;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadPoolUtil {

    public static ScheduledThreadPoolExecutor getScheduledExecutor(int coreSize, String groupName) {
        return new ScheduledThreadPoolExecutor(5, new DefaultThreadFactory(groupName));
    }
}
