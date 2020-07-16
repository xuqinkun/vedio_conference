package service.schedule;

import java.util.concurrent.atomic.AtomicBoolean;

public class TaskHolder<T> {
    private T task;
    private volatile boolean started;
    private volatile AtomicBoolean submitted;

    public TaskHolder(T task) {
        this.task = task;
        started = false;
        submitted = new AtomicBoolean(false);
    }

    public T getTask() {
        return task;
    }

    public boolean isStarted() {
        return started;
    }

    public synchronized void submit() {
        if (!submitted.get()) {
            TaskStarter.submit(this);
            submitted.getAndSet(true);
        }
    }

    public boolean isSubmitted() {
        return submitted.get();
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}
