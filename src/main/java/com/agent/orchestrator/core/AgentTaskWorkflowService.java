
package com.agent.orchestrator.core;

import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Service
public class AgentTaskWorkflowService {

    private final AgentRegistrationService registrationService;
    private final AgentTaskExecutionService executionService;

    public AgentTaskWorkflowService(
            AgentRegistrationService registrationService,
            AgentTaskExecutionService executionService) {

        this.registrationService = registrationService;
        this.executionService = executionService;
    }

    public <T> Future<T> submitAgentTask(
            String agentId,
            Callable<T> task) {

        if (agentId == null || agentId.isBlank()) {
            throw new IllegalArgumentException(
                    "Agent ID is required"
            );
        }

        Objects.requireNonNull(task, "Task cannot be null");

        RegisteredAgent agent = registrationService
                .getAgent(agentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Agent not found: " + agentId
                ));

        return executionService.submitTask(task);
    }
}
