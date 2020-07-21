package service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public class SlowTaskHolder<T> {

    private static final Logger log = LoggerFactory.getLogger(SlowTaskHolder.class);

    private T content;
    private volatile AtomicBoolean started;
    private volatile AtomicBoolean submitted;
    private volatile AtomicBoolean stopped;
    private final String taskName;

    public SlowTaskHolder(T content, String taskName) {
        this.content = content;
        started = new AtomicBoolean(false);
        submitted = new AtomicBoolean(false);
        stopped = new AtomicBoolean(false);
        this.taskName = taskName;
    }

    public T getContent() {
        return content;
    }

    public boolean isStarted() {
        return started.get();
    }

    public void submit(boolean force) {
        if (!submitted.get()) {
            log.debug("[{}] submit", taskName);
            TaskStarter.submit(this);
            submitted.getAndSet(true);
        } else if (force) {
            log.debug("[{}] already submitted, force to submit", taskName);
            TaskStarter.submit(this);
        } else {
            log.debug("[{}] already submitted, don't submit again", taskName);
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

    public void stop() {
        log.warn("Stop {}", taskName);
        TaskStarter.stop(this);
        stopped.getAndSet(true);
    }

    public boolean isStopped() {
        return stopped.get();
    }
}
