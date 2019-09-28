package com.sapozhnikov.executor;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertTrue;

public class LikeExecutorTest {

    private LikeExecutor likeExecutor;

    @Before
    public void init() {
        likeExecutor = new LikeExecutor();
    }

    @Test
    public void testAddAndRunTask() throws InterruptedException {
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LocalDateTime timeRunTask = LocalDateTime.now().plusNanos(100);

        likeExecutor.addTask(timeRunTask, () -> {
                    atomicBoolean.set(true);
                    String message = "completed!";
                    System.out.println(message);
                    return message;
                }
        );

        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(atomicBoolean.get());
    }

    @Test
    public void testAddAndRunTwoTask() throws InterruptedException {
        AtomicBoolean firstTaskTester = new AtomicBoolean(false);
        AtomicBoolean secondTaskTester = new AtomicBoolean(false);
        LocalDateTime timeRunFirstTask = LocalDateTime.now().plusNanos(100);
        LocalDateTime timeRunSecondTask = LocalDateTime.now().plusNanos(200);

        likeExecutor.addTask(timeRunFirstTask, () -> {
                    firstTaskTester.set(true);
                    String message = "completed first task!";
                    System.out.println(message);
                    return message;
                }
        );
        likeExecutor.addTask(timeRunSecondTask, () -> {
                    if (firstTaskTester.get()) {
                        secondTaskTester.set(true);
                    }
                    String message = "completed second task!";
                    System.out.println(message);
                    return message;
                }
        );

        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(firstTaskTester.get());
        assertTrue(secondTaskTester.get());
    }

    @Test
    public void testAddTwoTaskInvertOrderAndRunCorrectOrder() throws InterruptedException {
        AtomicBoolean firstTaskTester = new AtomicBoolean(false);
        AtomicBoolean secondTaskTester = new AtomicBoolean(false);
        LocalDateTime timeRunFirstTask = LocalDateTime.now().plusNanos(100);
        LocalDateTime timeRunSecondTask = LocalDateTime.now().plusNanos(200);

        likeExecutor.addTask(timeRunSecondTask, () -> {
                    if (firstTaskTester.get()) {
                        secondTaskTester.set(true);
                    }
                    String message = "completed second task!";
                    System.out.println(message);
                    return message;
                }
        );
        likeExecutor.addTask(timeRunFirstTask, () -> {
                    firstTaskTester.set(true);
                    String message = "completed first task!";
                    System.out.println(message);
                    return message;
                }
        );

        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(firstTaskTester.get());
        assertTrue(secondTaskTester.get());
    }

    @Test
    public void testAddAndRunTwoTaskWithEqualsTimeRun() throws InterruptedException {
        AtomicBoolean firstTaskTester = new AtomicBoolean(false);
        AtomicBoolean secondTaskTester = new AtomicBoolean(false);
        LocalDateTime timeRunBothTask = LocalDateTime.now().plusNanos(100);

        likeExecutor.addTask(timeRunBothTask, () -> {
                    firstTaskTester.set(true);
                    String message = "completed first task!";
                    System.out.println(message);
                    return message;
                }
        );
        likeExecutor.addTask(timeRunBothTask, () -> {
                    if (firstTaskTester.get()) {
                        secondTaskTester.set(true);
                    }
                    String message = "completed second task!";
                    System.out.println(message);
                    return message;
                }
        );

        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(firstTaskTester.get());
        assertTrue(secondTaskTester.get());
    }

    @Test
    public void testAddAndRunTwoTaskWithAddTaskFromDifferentThreads() throws InterruptedException {
        AtomicBoolean firstTaskTester = new AtomicBoolean(false);
        AtomicBoolean secondTaskTester = new AtomicBoolean(false);
        LocalDateTime timeRunFirstTask = LocalDateTime.now().plusNanos(100);
        LocalDateTime timeRunSecondTask = LocalDateTime.now().plusNanos(200);

        Thread threadForAddFirstTask = new Thread(() ->
                likeExecutor.addTask(timeRunFirstTask, () -> {
                            firstTaskTester.set(true);
                            String message = "completed first task!";
                            System.out.println(message);
                            return message;
                        }
                ));
        Thread threadForAddSecondTask = new Thread(() ->
                likeExecutor.addTask(timeRunSecondTask, () -> {
                            if (firstTaskTester.get()) {
                                secondTaskTester.set(true);
                            }
                            String message = "completed second task!";
                            System.out.println(message);
                            return message;
                        }
                ));
        threadForAddFirstTask.start();
        threadForAddSecondTask.start();

        TimeUnit.MILLISECONDS.sleep(100);
        assertTrue(firstTaskTester.get());
        assertTrue(secondTaskTester.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotAddOutdatedTask() {
        LocalDateTime timeRunTask = LocalDateTime.now().minusSeconds(1);

        likeExecutor.addTask(timeRunTask, null);
    }
}