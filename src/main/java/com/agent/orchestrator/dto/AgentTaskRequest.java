
package com.agent.orchestrator.dto;

public record AgentTaskRequest(
        String taskName,
        long durationMillis
) {
}
