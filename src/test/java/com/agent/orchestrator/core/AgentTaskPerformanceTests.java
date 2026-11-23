
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskPerformanceTests {

    @Test
    void evaluatesPerformanceUnderDifferentWorkloads() throws Exception {

        int[] workloadSizes = {50, 100, 250, 500};

        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {

            AgentTaskExecutionService service =
                    new AgentTaskExecutionService(executor);

            System.out.println("\n===== PERFORMANCE EVALUATION =====");

            for (int taskCount : workloadSizes) {

                List<Future<Long>> futures = new ArrayList<>();

                AtomicInteger activeTasks = new AtomicInteger(0);
                AtomicInteger peakConcurrentTasks = new AtomicInteger(0);

                Runtime runtime = Runtime.getRuntime();

                System.gc();
                long memoryBefore =
                        runtime.totalMemory() - runtime.freeMemory();

                long startTime = System.nanoTime();

                for (int i = 0; i < taskCount; i++) {

                    futures.add(service.submitTask(() -> {

                        int active = activeTasks.incrementAndGet();

                        peakConcurrentTasks.updateAndGet(
                                current -> Math.max(current, active)
                        );

                        long taskStart = System.nanoTime();

                        try {
                            Thread.sleep(50);
                            return System.nanoTime() - taskStart;
                        } finally {
                            activeTasks.decrementAndGet();
                        }
                    }));
                }

                long totalTaskLatency = 0;

                for (Future<Long> future : futures) {
                    totalTaskLatency += future.get(30, TimeUnit.SECONDS);
                }

                long elapsedTime = System.nanoTime() - startTime;

                long elapsedMillis =
                        TimeUnit.NANOSECONDS.toMillis(elapsedTime);

                double throughput =
                        taskCount / (elapsedTime / 1_000_000_000.0);

                double averageLatency =
                        TimeUnit.NANOSECONDS.toMillis(totalTaskLatency)
                                / (double) taskCount;

                long memoryAfter =
                        runtime.totalMemory() - runtime.freeMemory();

                long memoryUsed =
                        (memoryAfter - memoryBefore) / (1024 * 1024);

                System.out.println("\nWorkload: " + taskCount + " tasks");
                System.out.println("Execution time: " + elapsedMillis + " ms");

                System.out.printf(
                        "Throughput: %.2f tasks/second%n",
                        throughput
                );

                System.out.printf(
                        "Average task latency: %.2f ms%n",
                        averageLatency
                );

                System.out.println(
                        "Peak concurrent tasks: " + peakConcurrentTasks.get()
                );

                System.out.println(
                        "Approx. memory change: " + memoryUsed + " MB"
                );

                assertEquals(taskCount, futures.size());
                assertTrue(elapsedMillis > 0);
                assertTrue(peakConcurrentTasks.get() > 0);
            }
        }

        System.out.println("\n===== EVALUATION COMPLETED =====");
    }
}
