
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskExecutionServiceTests {

    @Test
    void executesMultipleTasksConcurrently() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            int taskCount = 100;
            CountDownLatch latch = new CountDownLatch(taskCount);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < taskCount; i++) {
                futures.add(service.submitTask(() -> {
                    try {
                        Thread.sleep(100);
                        return Thread.currentThread().isVirtual();
                    } finally {
                        latch.countDown();
                    }
                }));
            }

            assertTrue(latch.await(10, TimeUnit.SECONDS));

            for (Future<Boolean> future : futures) {
                assertTrue(future.get());
            }
        }
    }
}
