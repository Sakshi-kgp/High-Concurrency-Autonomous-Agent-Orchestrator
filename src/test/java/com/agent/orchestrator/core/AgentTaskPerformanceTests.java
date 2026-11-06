
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskPerformanceTests {

    @Test
    void measuresConcurrentTaskExecution() throws Exception {
        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            int taskCount = 100;
            List<Future<Long>> futures = new ArrayList<>();

            long startTime = System.nanoTime();

            for (int i = 0; i < taskCount; i++) {
                futures.add(service.submitTask(() -> {
                    long taskStart = System.nanoTime();

                    Thread.sleep(50);

                    return System.nanoTime() - taskStart;
                }));
            }

            long totalTaskTime = 0;

            for (Future<Long> future : futures) {
                totalTaskTime += future.get(10, TimeUnit.SECONDS);
            }

            long elapsedTime = System.nanoTime() - startTime;

            long elapsedMillis =
                    TimeUnit.NANOSECONDS.toMillis(elapsedTime);

            double throughput =
                    taskCount / (elapsedTime / 1_000_000_000.0);

            double averageTaskTime =
                    TimeUnit.NANOSECONDS.toMillis(totalTaskTime)
                            / (double) taskCount;

            System.out.println("----- Performance Results -----");
            System.out.println("Tasks completed: " + taskCount);
            System.out.println("Total execution time: " + elapsedMillis + " ms");
            System.out.printf("Throughput: %.2f tasks/second%n", throughput);
            System.out.printf("Average task time: %.2f ms%n", averageTaskTime);

            assertEquals(taskCount, futures.size());
            assertTrue(elapsedMillis > 0);
        }
    }
}
