package service.schedule;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeviceStarter {
    private static final Logger LOG = LoggerFactory.getLogger(DeviceStarter.class);

    private static final ScheduledExecutorService exec =
            Executors.newScheduledThreadPool(5, new DefaultThreadFactory("DeviceStarter-"));

    public static <T> void submit(DeviceHolder<T> holder) {
        if (holder != null && holder.getDevice() != null){
            try {
                T device = holder.getDevice();
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

    private static void printException(NoSuchMethodException e) {
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
    private static <T> void startUnsafe(DeviceHolder<T> holder) throws NoSuchMethodException {
        T device = holder.getDevice();
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

    private static <T> void start(DeviceHolder<T> holder) throws NoSuchMethodException {
        T device = holder.getDevice();
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
}
