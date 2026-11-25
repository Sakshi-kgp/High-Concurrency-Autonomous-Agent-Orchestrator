package com.agent.orchestrator.dto;

public record AgentRegistrationResponse(
        boolean success,
        String message,
        String agentId
) {}
