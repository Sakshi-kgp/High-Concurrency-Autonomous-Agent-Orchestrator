package com.agent.orchestrator.registry;

public record RegisteredAgent(
        String agentId,
        String agentName,
        String agentType,
        String agentAddress
) {
}