package service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceHolder<T> {

    private static final Logger log = LoggerFactory.getLogger(DeviceHolder.class);

    private T device;
    private volatile AtomicBoolean started;
    private volatile AtomicBoolean submitted;
    private final String taskName;

    public DeviceHolder(T device, String taskName) {
        this.device = device;
        started = new AtomicBoolean(false);
        submitted = new AtomicBoolean(false);
        this.taskName = taskName;
    }

    public T getDevice() {
        return device;
    }

    public boolean isStarted() {
        return started.get();
    }

    public void submit() {
        if (!submitted.get()) {
            log.warn("[{}] submit", taskName);
            DeviceStarter.submit(this);
            submitted.getAndSet(true);
        }
    }

    public boolean isSubmitted() {
        return submitted.get();
    }

    public void setStarted() {
        log.warn("[{}] started", taskName);
        started.getAndSet(true);
    }

    @Override
    public String toString() {
        return taskName;
    }

    public String getTaskName() {
        return taskName;
    }
}
