
package com.agent.orchestrator.controller;

import com.agent.orchestrator.core.AgentTaskWorkflowService;
import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.dto.AgentRegistrationResponse;
import com.agent.orchestrator.dto.AgentTaskRequest;
import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRegistrationService registrationService;
    private final AgentTaskWorkflowService workflowService;

    public AgentController(
            AgentRegistrationService registrationService,
            AgentTaskWorkflowService workflowService) {
        this.registrationService = registrationService;
        this.workflowService = workflowService;
    }

    @PostMapping("/register")
    public ResponseEntity<AgentRegistrationResponse> registerAgent(
            @RequestBody AgentRegistrationRequest request) {

        try {
            boolean registered = registrationService.registerAgent(request);

            if (registered) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new AgentRegistrationResponse(
                                true,
                                "Agent registered successfully",
                                request.agentId().trim()
                        ));
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AgentRegistrationResponse(
                            false,
                            "Agent ID already exists",
                            request.agentId().trim()
                    ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(new AgentRegistrationResponse(
                            false,
                            exception.getMessage(),
                            request == null ? "" : request.agentId()
                    ));
        }
    }

    @GetMapping("/{agentId}")
    public ResponseEntity<RegisteredAgent> getAgent(
            @PathVariable String agentId) {

        try {
            return registrationService.getAgent(agentId)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RegisteredAgent>> getAllAgents() {
        return ResponseEntity.ok(registrationService.getAllAgents());
    }

    @DeleteMapping("/{agentId}")
    public ResponseEntity<Void> deregisterAgent(
            @PathVariable String agentId) {

        try {
            boolean removed = registrationService.deregisterAgent(agentId);

            if (removed) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.notFound().build();

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{agentId}/tasks")
    public ResponseEntity<Map<String, Object>> executeAgentTask(
            @PathVariable String agentId,
            @RequestBody AgentTaskRequest request) {

        if (request == null
                || request.taskName() == null
                || request.taskName().isBlank()
                || request.durationMillis() <= 0
                || request.durationMillis() > 60000) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Valid task name and duration (1-60000 ms) are required"
            ));
        }

        Future<String> future = null;

        try {
            future = workflowService.submitAgentTask(
                    agentId,
                    () -> {
                        Thread.sleep(request.durationMillis());

                        return "Task '" + request.taskName()
                                + "' completed for agent " + agentId;
                    }
            );

            String result = future.get(
                    request.durationMillis() + 5000,
                    TimeUnit.MILLISECONDS
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "agentId", agentId,
                    "taskName", request.taskName(),
                    "result", result
            ));

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", exception.getMessage()
            ));

        } catch (TimeoutException exception) {
            if (future != null) {
                future.cancel(true);
            }

            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body(Map.of(
                            "success", false,
                            "message", "Task execution timed out"
                    ));

        } catch (InterruptedException exception) {
            if (future != null) {
                future.cancel(true);
            }

            Thread.currentThread().interrupt();

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "success", false,
                            "message", "Task execution was interrupted"
                    ));

        } catch (ExecutionException exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Task execution failed"
                    ));
        }
    }
}
