package com.agent.orchestrator.dashboard;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Service
public class DashboardMetricsService {

    private final AtomicLong activeTasks = new AtomicLong(0);
    private final LongAdder completedTasks = new LongAdder();
    private final LongAdder failedTasks = new LongAdder();
    private final LongAdder totalExecutionTime = new LongAdder();

    public void taskStarted() {
        activeTasks.incrementAndGet();
    }

    public void taskCompleted(long executionTimeMillis) {
        activeTasks.decrementAndGet();
        completedTasks.increment();
        totalExecutionTime.add(executionTimeMillis);
    }

    public void taskFailed(long executionTimeMillis) {
        activeTasks.decrementAndGet();
        failedTasks.increment();
        totalExecutionTime.add(executionTimeMillis);
    }

    public long getActiveTasks() {
        return activeTasks.get();
    }

    public long getCompletedTasks() {
        return completedTasks.sum();
    }

    public long getFailedTasks() {
        return failedTasks.sum();
    }

    public long getTotalTasks() {
        return completedTasks.sum() + failedTasks.sum();
    }

    public double getAverageExecutionTime() {
        long total = getTotalTasks();

        if (total == 0) {
            return 0;
        }

        return (double) totalExecutionTime.sum() / total;
    }
}
