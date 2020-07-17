package service.schedule;

import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceHolder<T> {
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

    public synchronized void submit() {
        if (!submitted.get()) {
            DeviceStarter.submit(this);
            submitted.getAndSet(true);
        }
    }

    public boolean isSubmitted() {
        return submitted.get();
    }

    public synchronized void setStarted() {
        started.getAndSet(true);
    }

    @Override
    public String toString() {
        return taskName;
    }
}
