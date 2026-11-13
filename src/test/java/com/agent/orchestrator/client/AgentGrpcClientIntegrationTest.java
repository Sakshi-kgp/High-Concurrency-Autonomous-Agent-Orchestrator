
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

    @Test
    void clientShouldHandleUnavailableServer() {

        AgentGrpcClient client = new AgentGrpcClient("localhost", 9191);

        try {
            AgentRegistrationResponse response = client.registerAgent(
                    "test-failure",
                    "Failure Test Agent",
                    "WORKER",
                    "localhost:8082"
            );

            assertFalse(response.getSuccess());
            assertTrue(response.getMessage().contains(
                    "gRPC communication failed"
            ));

        } finally {
            client.shutdown();
        }
    }

    @Test
    void clientShouldRejectInvalidRegistration() {

        String agentId = "invalid-" + UUID.randomUUID();

        AgentGrpcClient client = new AgentGrpcClient("localhost", 9090);

        try {
            AgentRegistrationResponse response = client.registerAgent(
                    agentId,
                    "",
                    "WORKER",
                    "localhost:8083"
            );

            assertFalse(response.getSuccess());
            assertEquals(
                    "Registration failed: All fields are required",
                    response.getMessage()
            );
            assertTrue(registry.findById(agentId).isEmpty());

        } finally {
            client.shutdown();
        }
    }

    @Test
    void clientShouldRejectDuplicateRegistration() {

        String agentId = "duplicate-" + UUID.randomUUID();

        AgentGrpcClient client = new AgentGrpcClient("localhost", 9090);

        try {
            AgentRegistrationResponse firstResponse = client.registerAgent(
                    agentId,
                    "First Agent",
                    "WORKER",
                    "localhost:8084"
            );

            AgentRegistrationResponse secondResponse = client.registerAgent(
                    agentId,
                    "Second Agent",
                    "WORKER",
                    "localhost:8085"
            );

            assertTrue(firstResponse.getSuccess());
            assertFalse(secondResponse.getSuccess());
            assertEquals(
                    "Registration failed: Agent ID already exists",
                    secondResponse.getMessage()
            );
            assertTrue(registry.findById(agentId).isPresent());

        } finally {
            client.shutdown();
        }
    }
}
