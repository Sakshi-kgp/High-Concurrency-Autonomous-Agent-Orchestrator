package com.agent.orchestrator.controller;

import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.dto.AgentRegistrationResponse;
import com.agent.orchestrator.service.AgentRegistrationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentRegistrationService registrationService;

    public AgentController(AgentRegistrationService registrationService) {
        this.registrationService = registrationService;
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
}
