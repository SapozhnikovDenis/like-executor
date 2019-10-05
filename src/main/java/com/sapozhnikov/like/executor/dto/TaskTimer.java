package com.sapozhnikov.like.executor.dto;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;

public class TaskTimer {
    private final LocalDateTime timeRunTask;
    private final Callable task;

    public TaskTimer(LocalDateTime timeRunTask, Callable task) {
        this.timeRunTask = timeRunTask;
        this.task = task;
    }

    public LocalDateTime getTimeRunTask() {
        return timeRunTask;
    }

    public Callable getTask() {
        return task;
    }
}
