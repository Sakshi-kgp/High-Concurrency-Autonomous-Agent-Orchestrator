
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class AgentTaskPerformanceTests {

    private record BenchmarkResult(
            String executorType,
            int taskCount,
            long executionTimeMs,
            double throughput,
            double averageLatencyMs,
            int peakConcurrency,
            long memoryChangeMB
    ) {
    }

    @Test
    void comparesVirtualThreadsAndPlatformThreads() throws Exception {

        int[] workloadSizes = {50, 100, 250, 500};

        VirtualThreadConfig config = new VirtualThreadConfig();

        System.out.println("\n========== THREAD PERFORMANCE COMPARISON ==========");

        for (int taskCount : workloadSizes) {

            BenchmarkResult virtualResult;

            try (ExecutorService executor = config.agentTaskExecutor()) {
                virtualResult = benchmark(
                        executor,
                        "Virtual Threads",
                        taskCount
                );
            }

            int platformPoolSize = Math.max(
                    4,
                    Runtime.getRuntime().availableProcessors() * 2
            );

            BenchmarkResult platformResult;

            try (ExecutorService executor =
                         Executors.newFixedThreadPool(platformPoolSize)) {

                platformResult = benchmark(
                        executor,
                        "Platform Threads",
                        taskCount
                );
            }

            printComparison(virtualResult, platformResult);

            assertEquals(taskCount, virtualResult.taskCount());
            assertEquals(taskCount, platformResult.taskCount());
            assertTrue(virtualResult.executionTimeMs() > 0);
            assertTrue(platformResult.executionTimeMs() > 0);
            assertTrue(virtualResult.peakConcurrency() > 0);
            assertTrue(platformResult.peakConcurrency() > 0);
        }

        System.out.println("\n========== EVALUATION COMPLETED ==========");
    }

    private BenchmarkResult benchmark(
            ExecutorService executor,
            String executorType,
            int taskCount
    ) throws Exception {

        List<Future<Long>> futures = new ArrayList<>();

        AtomicInteger activeTasks = new AtomicInteger();
        AtomicInteger peakConcurrency = new AtomicInteger();

        Runtime runtime = Runtime.getRuntime();

        System.gc();
        Thread.sleep(100);

        long memoryBefore =
                runtime.totalMemory() - runtime.freeMemory();

        long benchmarkStart = System.nanoTime();

        for (int i = 0; i < taskCount; i++) {

            long submissionTime = System.nanoTime();

            futures.add(executor.submit(() -> {

                int active = activeTasks.incrementAndGet();

                peakConcurrency.updateAndGet(
                        current -> Math.max(current, active)
                );

                try {
                    Thread.sleep(50);

                    return System.nanoTime() - submissionTime;

                } finally {
                    activeTasks.decrementAndGet();
                }
            }));
        }

        long totalLatencyNanos = 0;

        for (Future<Long> future : futures) {
            totalLatencyNanos += future.get(60, TimeUnit.SECONDS);
        }

        long elapsedNanos = System.nanoTime() - benchmarkStart;

        long memoryAfter =
                runtime.totalMemory() - runtime.freeMemory();

        long executionTimeMs =
                TimeUnit.NANOSECONDS.toMillis(elapsedNanos);

        double throughput =
                taskCount / (elapsedNanos / 1_000_000_000.0);

        double averageLatencyMs =
                totalLatencyNanos / (double) taskCount / 1_000_000.0;

        long memoryChangeMB =
                (memoryAfter - memoryBefore) / (1024 * 1024);

        return new BenchmarkResult(
                executorType,
                taskCount,
                executionTimeMs,
                throughput,
                averageLatencyMs,
                peakConcurrency.get(),
                memoryChangeMB
        );
    }

    private void printComparison(
            BenchmarkResult virtual,
            BenchmarkResult platform
    ) {

        System.out.println("\nWorkload: " + virtual.taskCount() + " tasks");

        printResult(virtual);
        printResult(platform);
    }

    private void printResult(BenchmarkResult result) {

        System.out.println("\nExecutor: " + result.executorType());

        System.out.println(
                "Execution time: " + result.executionTimeMs() + " ms"
        );

        System.out.printf(
                "Throughput: %.2f tasks/second%n",
                result.throughput()
        );

        System.out.printf(
                "Average end-to-end latency: %.2f ms%n",
                result.averageLatencyMs()
        );

        System.out.println(
                "Peak concurrency: " + result.peakConcurrency()
        );

        System.out.println(
                "Approx. memory change: " + result.memoryChangeMB() + " MB"
        );
    }
}
