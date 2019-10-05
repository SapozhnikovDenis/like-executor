package com.sapozhnikov.like.executor;

import com.sapozhnikov.like.executor.dto.TaskTimer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Logger;

public class LikeExecutor {

    private final Logger log = Logger.getLogger(LikeExecutor.class.getName());

    private final Thread taskExecutor;
    private final Thread taskQueueToPullMover;
    private final LinkedBlockingQueue<TaskTimer> taskTimerQueue;
    private final ConcurrentSkipListMap<LocalDateTime, Queue<Callable>> taskPull;

    private static final ZoneOffset SYSTEM_ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public LikeExecutor() {
        this.taskPull = new ConcurrentSkipListMap<>();
        this.taskTimerQueue = new LinkedBlockingQueue<>();
        this.taskExecutor = new Thread(this::getAndRunTask, "TASK_EXECUTOR");
        this.taskExecutor.setDaemon(true);
        this.taskExecutor.start();
        this.taskQueueToPullMover = new Thread(this::moveTaskFromQueueToPull, "TASK_QUEUE_TO_PULL_MOVER");
        this.taskQueueToPullMover.setDaemon(true);
        this.taskQueueToPullMover.start();
    }

    public void addTask(LocalDateTime timeRunTask, Callable task) {
        if (LocalDateTime.now().isAfter(timeRunTask)) {
            throw new IllegalArgumentException("task outdated!");
        }
        TaskTimer taskTimer = new TaskTimer(timeRunTask, task);
        log.info(String.format("taskTimer=%s add to taskTimerQueue", taskTimer));
        taskTimerQueue.add(taskTimer);
        LockSupport.unpark(taskQueueToPullMover);
    }

    private void moveTaskFromQueueToPull() {
        while (true) {
            if (taskTimerQueue.isEmpty()) {
                log.info("taskTimerQueue! taskTimerQueue notify");
                LockSupport.park(taskQueueToPullMover);
                continue;
            }
            TaskTimer taskTimer = taskTimerQueue.poll();
            putToPull(taskTimer);
        }
    }

    private void putToPull(TaskTimer taskTimer) {
        LocalDateTime timeRunTask = taskTimer.getTimeRunTask();
        Callable task = taskTimer.getTask();
        if (taskPull.containsKey(timeRunTask)) {
            Queue<Callable> taskQueue = taskPull.get(timeRunTask);
            taskQueue.add(task);
        } else {
            Queue<Callable> taskQueue = new ConcurrentLinkedQueue<>();
            taskQueue.add(task);
            taskPull.put(timeRunTask, taskQueue);
        }
        log.info(String.format("callable with date=%s put in taskPull!", timeRunTask));
        taskExecutor.interrupt();
    }

    private void getAndRunTask() {
        while (true) {
            try {
                LocalDateTime dataTimeNextTask;
                try {
                    dataTimeNextTask = taskPull.firstKey();
                } catch (NoSuchElementException e1) {
                    log.info("taskPoolIsEmpty! taskExecutor notify");
                    LockSupport.park(taskExecutor);
                    continue;
                }
                Thread.sleep(calculateMillisecondToDateTime(dataTimeNextTask));
                taskPull.pollFirstEntry()
                        .getValue()
                        .forEach(callable -> runTask(dataTimeNextTask, callable));
            } catch (InterruptedException e) {
                log.info("taskExecutor wake up");
            }
        }
    }

    private void runTask(LocalDateTime dataTimeNextTask, Callable callable) {
        try {
            log.info(String.format("callable with date=%s start work!", dataTimeNextTask));
            Object call = callable.call();
            log.info(String.format("callable with date=%s complete! Return=%s", dataTimeNextTask, call));
        } catch (Exception e) {
            log.info(String.format("callable with date=%s throw exception!", dataTimeNextTask));
            e.printStackTrace();
        }
    }

    private long calculateMillisecondToDateTime(LocalDateTime dataTimeNextTask) {
        long difference = dataTimeNextTask.toInstant(SYSTEM_ZONE_OFFSET).toEpochMilli()
                - LocalDateTime.now().toInstant(SYSTEM_ZONE_OFFSET).toEpochMilli();
        return 0 < difference ? difference : 0;
    }
}
