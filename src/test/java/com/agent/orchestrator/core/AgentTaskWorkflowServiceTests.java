
package com.agent.orchestrator.core;

import com.agent.orchestrator.config.VirtualThreadConfig;
import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgentTaskWorkflowServiceTests {

    @Test
    void executesTaskForRegisteredAgent() throws Exception {
        AgentRegistrationService registrationService =
                mock(AgentRegistrationService.class);

        RegisteredAgent agent = new RegisteredAgent(
                "agent-101",
                "Research Agent",
                "AI",
                "localhost:50051"
        );

        when(registrationService.getAgent("agent-101"))
                .thenReturn(Optional.of(agent));

        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService executionService =
                    new AgentTaskExecutionService(executor);

            AgentTaskWorkflowService workflowService =
                    new AgentTaskWorkflowService(
                            registrationService,
                            executionService
                    );

            Future<String> result = workflowService.submitAgentTask(
                    "agent-101",
                    () -> "Agent task completed"
            );

            assertEquals(
                    "Agent task completed",
                    result.get(5, TimeUnit.SECONDS)
            );
        }
    }

    @Test
    void rejectsUnregisteredAgent() {
        AgentRegistrationService registrationService =
                mock(AgentRegistrationService.class);

        when(registrationService.getAgent("unknown"))
                .thenReturn(Optional.empty());

        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService executionService =
                    new AgentTaskExecutionService(executor);

            AgentTaskWorkflowService workflowService =
                    new AgentTaskWorkflowService(
                            registrationService,
                            executionService
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> workflowService.submitAgentTask(
                            "unknown",
                            () -> "Should not execute"
                    )
            );
        }
    }

    @Test
    void rejectsBlankAgentId() {
        AgentRegistrationService registrationService =
                mock(AgentRegistrationService.class);

        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService executionService =
                    new AgentTaskExecutionService(executor);

            AgentTaskWorkflowService workflowService =
                    new AgentTaskWorkflowService(
                            registrationService,
                            executionService
                    );

            assertThrows(
                    IllegalArgumentException.class,
                    () -> workflowService.submitAgentTask(
                            " ",
                            () -> "Task"
                    )
            );
        }
    }

    @Test
    void rejectsNullTask() {
        AgentRegistrationService registrationService =
                mock(AgentRegistrationService.class);

        VirtualThreadConfig config = new VirtualThreadConfig();

        try (ExecutorService executor = config.agentTaskExecutor()) {
            AgentTaskExecutionService executionService =
                    new AgentTaskExecutionService(executor);

            AgentTaskWorkflowService workflowService =
                    new AgentTaskWorkflowService(
                            registrationService,
                            executionService
                    );

            assertThrows(
                    NullPointerException.class,
                    () -> workflowService.submitAgentTask(
                            "agent-101",
                            null
                    )
            );
        }
    }
}
