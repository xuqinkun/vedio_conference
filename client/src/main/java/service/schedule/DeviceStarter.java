package service.schedule;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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
        new DeviceStartupService<>(holder).start();
    }

    static class DeviceStartupService<T> extends Service<Boolean> {
        private DeviceHolder<T> holder;

        public DeviceStartupService(DeviceHolder<T> holder) {
            this.holder = holder;
            valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    holder.setStarted();
                }
            });
        }

        @Override
        protected Task<Boolean> createTask() {
            return new Task<Boolean>() {
                @Override
                protected Boolean call() {
                    T device = holder.getDevice();
                    try {
                        Method startMethod = device.getClass().getMethod("start");
                        if (startMethod != null) {
                            exec.schedule(() -> {
                                try {
                                    LOG.warn("Device[{}] starting...", holder);
                                    startMethod.invoke(device);
                                    holder.setStarted();
                                    LOG.warn("Device[{}] started", holder);
                                } catch (Exception e) {
                                    LOG.error("Device[{}] failed to start. Error: {}", holder, e.getMessage());
                                }
                            }, 0, TimeUnit.MILLISECONDS);
                        }
                        return true;
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
        }
    }
}
