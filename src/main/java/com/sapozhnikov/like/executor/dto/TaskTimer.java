package com.sapozhnikov.like.executor.dto;

import java.time.LocalDateTime;
import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskTimer taskTimer = (TaskTimer) o;
        return Objects.equals(timeRunTask, taskTimer.timeRunTask) &&
                Objects.equals(task, taskTimer.task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeRunTask, task);
    }

    @Override
    public String toString() {
        return "TaskTimer{" +
                "timeRunTask=" + timeRunTask +
                ", task=" + task +
                '}';
    }
}
