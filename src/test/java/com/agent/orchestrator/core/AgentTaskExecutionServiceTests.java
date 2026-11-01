
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
            assertTrue(completed.await(10, TimeUnit.SECONDS),
                    "All tasks should complete within 10 seconds");

            for (Future<Boolean> future : futures) {
                assertTrue(future.get());
            }

            assertTrue(maxConcurrentTasks.get() > 1,
                    "Multiple tasks should execute concurrently");
        }
    }
}
