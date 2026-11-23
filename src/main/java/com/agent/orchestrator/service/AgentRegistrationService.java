package com.agent.orchestrator.service;

import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.registry.AgentRegistry;
import com.agent.orchestrator.registry.RegisteredAgent;
import org.springframework.stereotype.Service;

@Service
public class AgentRegistrationService {

    private final AgentRegistry registry;

    public AgentRegistrationService(AgentRegistry registry) {
        this.registry = registry;
    }

    public boolean registerAgent(AgentRegistrationRequest request) {

        if (request == null
                || request.agentId() == null || request.agentId().isBlank()
                || request.agentName() == null || request.agentName().isBlank()
                || request.agentType() == null || request.agentType().isBlank()
                || request.agentAddress() == null || request.agentAddress().isBlank()) {

            throw new IllegalArgumentException("All fields are required");
        }

        RegisteredAgent agent = new RegisteredAgent(
                request.agentId().trim(),
                request.agentName().trim(),
                request.agentType().trim(),
                request.agentAddress().trim()
        );

        return registry.register(agent);
    }
}
