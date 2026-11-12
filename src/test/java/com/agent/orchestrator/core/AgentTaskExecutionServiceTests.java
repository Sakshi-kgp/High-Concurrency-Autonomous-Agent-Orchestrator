
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskExecutionServiceTests {

    @Test
    void executesMultipleTasksConcurrently() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            int taskCount = 100;

            CountDownLatch ready = new CountDownLatch(taskCount);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch completed = new CountDownLatch(taskCount);

            AtomicInteger activeTasks = new AtomicInteger(0);
            AtomicInteger maxConcurrentTasks = new AtomicInteger(0);

            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < taskCount; i++) {
                futures.add(service.submitTask(() -> {
                    int active = activeTasks.incrementAndGet();

                    maxConcurrentTasks.updateAndGet(
                            current -> Math.max(current, active)
                    );

                    ready.countDown();

                    try {
                        start.await();
                        Thread.sleep(100);
                        return Thread.currentThread().isVirtual();
                    } finally {
                        activeTasks.decrementAndGet();
                        completed.countDown();
                    }
                }));
            }

            boolean allReady = ready.await(10, TimeUnit.SECONDS);
            start.countDown();

            assertTrue(allReady, "All tasks should start within 10 seconds");

            assertTrue(
                    completed.await(10, TimeUnit.SECONDS),
                    "All tasks should complete within 10 seconds"
            );

            for (Future<Boolean> future : futures) {
                assertTrue(future.get());
            }

            assertTrue(
                    maxConcurrentTasks.get() > 1,
                    "Multiple tasks should execute concurrently"
            );
        }
    }

    @Test
    void rejectsNullTask() {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            assertThrows(
                    NullPointerException.class,
                    () -> service.submitTask(null)
            );
        }
    }

    @Test
    void returnsTaskResult() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            Future<String> result =
                    service.submitTask(() -> "Task completed");

            assertEquals(
                    "Task completed",
                    result.get(5, TimeUnit.SECONDS)
            );
        }
    }

    @Test
    void propagatesTaskException() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            Future<String> result = service.submitTask(() -> {
                throw new IllegalStateException("Task failed");
            });

            ExecutionException exception = assertThrows(
                    ExecutionException.class,
                    () -> result.get(5, TimeUnit.SECONDS)
            );

            assertInstanceOf(
                    IllegalStateException.class,
                    exception.getCause()
            );

            assertEquals("Task failed", exception.getCause().getMessage());
        }
    }

    @Test
    void cancelsSubmittedTask() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            CountDownLatch taskStarted = new CountDownLatch(1);
            CountDownLatch taskCancelled = new CountDownLatch(1);

            Future<?> future = service.submitTask(() -> {
                taskStarted.countDown();

                try {
                    new CountDownLatch(1).await();
                } catch (InterruptedException e) {
                    taskCancelled.countDown();
                    Thread.currentThread().interrupt();
                }

                return null;
            });

            assertTrue(taskStarted.await(5, TimeUnit.SECONDS));
            assertTrue(future.cancel(true));
            assertTrue(future.isCancelled());
            assertTrue(taskCancelled.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void doesNotCancelCompletedTask() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            Future<String> future =
                    service.submitTask(() -> "Completed");

            assertEquals("Completed", future.get(5, TimeUnit.SECONDS));
            assertFalse(future.cancel(true));
            assertFalse(future.isCancelled());
        }
    }

    @Test
    void rejectsTasksAfterExecutorShutdown() {
        VirtualThreadConfig config = new VirtualThreadConfig();

        ExecutorService executor = config.agentTaskExecutor();
        AgentTaskExecutionService service =
                new AgentTaskExecutionService(executor);

        executor.shutdown();

        assertThrows(
                RejectedExecutionException.class,
                () -> service.submitTask(() -> "Should not execute")
        );
    }

    @Test
    void cancelsTaskWhenTimeoutOccurs() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            CountDownLatch taskStarted = new CountDownLatch(1);
            CountDownLatch taskCancelled = new CountDownLatch(1);

            assertThrows(
                    TimeoutException.class,
                    () -> service.executeTaskWithTimeout(() -> {
                        taskStarted.countDown();

                        try {
                            new CountDownLatch(1).await();
                        } catch (InterruptedException e) {
                            taskCancelled.countDown();
                            Thread.currentThread().interrupt();
                        }

                        return null;
                    }, 100, TimeUnit.MILLISECONDS)
            );

            assertTrue(taskStarted.await(5, TimeUnit.SECONDS));
            assertTrue(taskCancelled.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void completesTaskWithinTimeout() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            String result = service.executeTaskWithTimeout(
                    () -> "Completed",
                    5,
                    TimeUnit.SECONDS
            );

            assertEquals("Completed", result);
        }
    }

    @Test
    void rejectsInvalidTimeout() {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> service.executeTaskWithTimeout(
                            () -> "Task",
                            0,
                            TimeUnit.SECONDS
                    )
            );
        }
    }
}
