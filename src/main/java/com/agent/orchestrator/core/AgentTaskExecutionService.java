
package com.agent.orchestrator.core;

import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;

@Service
public class AgentTaskExecutionService {

    private final ExecutorService agentTaskExecutor;

    public AgentTaskExecutionService(ExecutorService agentTaskExecutor) {
        this.agentTaskExecutor = agentTaskExecutor;
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        Objects.requireNonNull(task, "Task cannot be null");
        return agentTaskExecutor.submit(task);
    }

    public <T> T executeTaskWithTimeout(
            Callable<T> task,
            long timeout,
            TimeUnit unit
    ) throws InterruptedException, ExecutionException, TimeoutException {

        Objects.requireNonNull(task, "Task cannot be null");
        Objects.requireNonNull(unit, "TimeUnit cannot be null");

        if (timeout <= 0) {
            throw new IllegalArgumentException(
                    "Timeout must be greater than zero"
            );
        }

        Future<T> future = submitTask(task);

        try {
            return future.get(timeout, unit);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw e;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw e;
        }
    }
}
