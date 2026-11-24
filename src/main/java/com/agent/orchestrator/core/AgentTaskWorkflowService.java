
package com.agent.orchestrator.core;

import com.agent.orchestrator.dashboard.DashboardMetricsService;
import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Service
public class AgentTaskWorkflowService {

    private final AgentRegistrationService registrationService;
    private final AgentTaskExecutionService executionService;
    private final DashboardMetricsService metricsService;

    @Autowired
    public AgentTaskWorkflowService(
            AgentRegistrationService registrationService,
            AgentTaskExecutionService executionService,
            DashboardMetricsService metricsService) {

        this.registrationService = registrationService;
        this.executionService = executionService;
        this.metricsService = metricsService;
    }

    public AgentTaskWorkflowService(
            AgentRegistrationService registrationService,
            AgentTaskExecutionService executionService) {

        this(
                registrationService,
                executionService,
                new DashboardMetricsService()
        );
    }

    public <T> Future<T> submitAgentTask(
            String agentId,
            Callable<T> task) {

        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException("Agent ID is required");
        }

        Objects.requireNonNull(task, "Task cannot be null");

        RegisteredAgent agent = registrationService
                .getAgent(agentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agent not found: " + agentId
                ));

        Callable<T> monitoredTask = () -> {
            long startTime = System.nanoTime();
            metricsService.taskStarted();

            try {
                T result = task.call();

                long duration = (System.nanoTime() - startTime)
                        / 1_000_000;

                metricsService.taskCompleted(duration);
                return result;

            } catch (Exception | Error exception) {
                long duration = (System.nanoTime() - startTime)
                        / 1_000_000;

                metricsService.taskFailed(duration);
                throw exception;
            }
        };

        return executionService.submitTask(monitoredTask);
    }
}
