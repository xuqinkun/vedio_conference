package service.schedule;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.DefaultThreadFactory;
import util.ThreadPoolUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskStarter {
    private static final Logger LOG = LoggerFactory.getLogger(TaskStarter.class);

    private static final ScheduledExecutorService exec = ThreadPoolUtil.getScheduledExecutor(5, "DeviceStarter");

    public static <T> void submit(SlowTaskHolder<T> holder) {
        if (holder != null && holder.getContent() != null){
            try {
                T device = holder.getContent();
                if (device instanceof FFmpegFrameGrabber || device instanceof FrameRecorder) {
                    startUnsafe(holder);
                } else {
                    start(holder);
                }
            } catch (NoSuchMethodException e) {
                printException(e);
            }
        }
    }

    private static void printException(Exception e) {
        if (e.getCause() != null) {
            LOG.error(e.getClass().toString());
        } else {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Start for FrameGrabber, set findStreamInfo to false to minimize startup time
     * Check whether the Network is available or not before call start method, otherwise the method will be blocked!
     * @param holder
     * @param <T>
     * @throws NoSuchMethodException
     */
    private static <T> void startUnsafe(SlowTaskHolder<T> holder) throws NoSuchMethodException {
        T device = holder.getContent();
        Method startMethod = device.getClass().getMethod("startUnsafe");
        if (startMethod != null) {
            exec.schedule(() -> {
                try {
                    LOG.debug("Device[{}] starting (startUnsafe)...", holder);
                    startMethod.invoke(device);
                    holder.setStarted();
                } catch (Exception e) {
                    String errMessage = printException(holder.toString(), e);
                    if (errMessage.contains("Did not find a video or audio stream inside")) {
                        holder.setStarted();
                    }
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    private static <T> String printException(String deviceName, Exception e) {
        String errMessage;
        if (e.getCause() != null) {
            errMessage = e.getCause().getMessage();
        }
        else {
            errMessage = e.getMessage();
        }
        LOG.error("Device[{}] failed to start. Error: {}", deviceName, errMessage);
        return errMessage;
    }

    private static <T> void start(SlowTaskHolder<T> holder) throws NoSuchMethodException {
        T device = holder.getContent();
        Method startMethod = device.getClass().getMethod("start");
        if (startMethod != null) {
            exec.schedule(() -> {
                try {
                    LOG.debug("Device[{}] starting...", holder);
                    startMethod.invoke(device);
                    holder.setStarted();
                } catch (Exception e) {
                    printException(holder.toString(), e);
                }
            }, 0, TimeUnit.MILLISECONDS);
        }
    }

    public static <T> void stop(SlowTaskHolder<T> taskHolder) {
        T content = taskHolder.getContent();
        try {
            Method stopMethod = content.getClass().getMethod("stop");
            if (stopMethod != null) {
                exec.submit(()->{
                    try {
                        stopMethod.invoke(content);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        printException(e);
                    }
                });
            }
        } catch (NoSuchMethodException e) {
            printException(e);
        }
    }
}
