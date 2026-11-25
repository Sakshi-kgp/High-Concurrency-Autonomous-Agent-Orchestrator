
package com.agent.orchestrator.registry;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentRegistry {

    private final ConcurrentHashMap<String, RegisteredAgent> agents =
            new ConcurrentHashMap<>();

    public boolean register(RegisteredAgent agent) {
        return agents.putIfAbsent(agent.agentId(), agent) == null;
    }

    public Optional<RegisteredAgent> findById(String agentId) {
        return Optional.ofNullable(agents.get(agentId));
    }

    public List<RegisteredAgent> findAll() {
        return List.copyOf(agents.values());
    }

    public boolean deregister(String agentId) {
        return agents.remove(agentId) != null;
    }

    public int count() {
        return agents.size();
    }
}
