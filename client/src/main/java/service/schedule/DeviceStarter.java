package service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceStarter {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceStarter.class);

    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(5);

    public static <T> void submit(DeviceHolder<T> holder) {
        if (holder == null || holder.getDevice() == null) {
            LOG.warn("Null task!");
            return;
        }
        T task = holder.getDevice();
        try {
            Method startMethod = task.getClass().getMethod("start");
            if (startMethod != null) {
                exec.schedule(() -> {
                    try {
                        LOG.warn("Task[{}] starting...", holder);
                        startMethod.invoke(task);
                        holder.setStarted();
                        LOG.warn("Task[{}] started", holder);
                    } catch (Exception e) {
                        LOG.error("Task[{}] failed to start. Error: {}", holder, e.getMessage());
                    }
                }, 0 , TimeUnit.MILLISECONDS);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
