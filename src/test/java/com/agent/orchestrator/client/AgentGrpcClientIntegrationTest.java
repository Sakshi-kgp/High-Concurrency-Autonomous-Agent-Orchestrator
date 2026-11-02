
package com.agent.orchestrator.client;

import com.agent.orchestrator.registry.AgentRegistry;
import com.orchestrator.grpc.AgentRegistrationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AgentGrpcClientIntegrationTest {

    @Autowired
    private AgentRegistry registry;

    @Test
    void clientShouldRegisterAgentThroughGrpcServer() {

        String agentId = "integration-" + UUID.randomUUID();

        AgentGrpcClient client = new AgentGrpcClient("localhost", 9090);

        try {
            AgentRegistrationResponse response = client.registerAgent(
                    agentId,
                    "Integration Test Agent",
                    "WORKER",
                    "localhost:8081"
            );

            assertTrue(response.getSuccess());
            assertEquals(agentId, response.getAgentId());
            assertTrue(registry.findById(agentId).isPresent());

        } finally {
            client.shutdown();
        }
    }
}
