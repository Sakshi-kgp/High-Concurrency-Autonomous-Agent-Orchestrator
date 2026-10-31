package com.agent.orchestrator.core;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class AgentTaskExecutionService {

    private final ExecutorService agentTaskExecutor;

    public AgentTaskExecutionService(ExecutorService agentTaskExecutor) {
        this.agentTaskExecutor = agentTaskExecutor;
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return agentTaskExecutor.submit(task);
    }
}
