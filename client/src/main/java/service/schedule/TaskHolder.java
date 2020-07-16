package service.schedule;

import java.util.concurrent.atomic.AtomicBoolean;

public class TaskHolder<T> {
    private T task;
    private volatile AtomicBoolean started;
    private volatile AtomicBoolean submitted;
    private final String taskName;

    public TaskHolder(T task, String taskName) {
        this.task = task;
        started = new AtomicBoolean(false);
        submitted = new AtomicBoolean(false);
        this.taskName = taskName;
    }

    public T getTask() {
        return task;
    }

    public synchronized boolean isStarted() {
        return started.get();
    }

    public synchronized void submit() {
        if (!submitted.get()) {
            TaskStarter.submit(this);
            submitted.getAndSet(true);
        }
    }

    public synchronized boolean isSubmitted() {
        return submitted.get();
    }

    public void setStarted() {
        this.started.getAndSet(true);
    }

    @Override
    public String toString() {
        return taskName;
    }
}
