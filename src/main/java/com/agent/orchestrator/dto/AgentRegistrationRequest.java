package com.agent.orchestrator.dto;

public record AgentRegistrationRequest(
        String agentId,
        String agentName,
        String agentType,
        String agentAddress
) {}
