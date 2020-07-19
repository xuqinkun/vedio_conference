package util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ThreadPoolUtil {

    public static ScheduledThreadPoolExecutor getScheduledExecutor(int coreSize, String groupName) {
        return new ScheduledThreadPoolExecutor(5, new DefaultThreadFactory(groupName));
    }

    public static ExecutorService getExecutorService(int coreSize, String groupName) {
        return Executors.newFixedThreadPool(coreSize, new DefaultThreadFactory(groupName));
    }
}
