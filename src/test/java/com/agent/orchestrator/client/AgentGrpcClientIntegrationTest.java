
package com.agent.orchestrator.client;

import com.agent.orchestrator.registry.AgentRegistry;
import com.orchestrator.grpc.AgentRegistrationResponse;
import io.grpc.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AgentGrpcClientIntegrationTest {

    @Autowired
    private AgentRegistry registry;

    @Autowired
    private Server grpcServer;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clientShouldRegisterAgentThroughGrpcServer() {
        String agentId = "integration-" + UUID.randomUUID();

        AgentGrpcClient client =
                new AgentGrpcClient("localhost", grpcServer.getPort());

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
            registry.deregister(agentId);
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
    void restRegisteredAgentShouldBeVisibleToGrpc() throws Exception {
        String agentId = "rest-" + UUID.randomUUID();

        String requestBody = """
                {
                    "agentId": "%s",
                    "agentName": "REST Test Agent",
                    "agentType": "WORKER",
                    "agentAddress": "localhost:8083"
                }
                """.formatted(agentId);

        AgentGrpcClient client =
                new AgentGrpcClient("localhost", grpcServer.getPort());

        try {
            mockMvc.perform(post("/api/agents/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated());

            AgentRegistrationResponse response = client.registerAgent(
                    agentId,
                    "Duplicate Agent",
                    "WORKER",
                    "localhost:8084"
            );

            assertFalse(response.getSuccess());
            assertTrue(registry.findById(agentId).isPresent());

        } finally {
            client.shutdown();
            registry.deregister(agentId);
        }
    }

    @Test
    void grpcRegisteredAgentShouldBeVisibleToRest() throws Exception {
        String agentId = "grpc-" + UUID.randomUUID();

        AgentGrpcClient client =
                new AgentGrpcClient("localhost", grpcServer.getPort());

        try {
            AgentRegistrationResponse response = client.registerAgent(
                    agentId,
                    "gRPC Test Agent",
                    "WORKER",
                    "localhost:8085"
            );

            assertTrue(response.getSuccess());

            mockMvc.perform(get("/api/agents/{agentId}", agentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.agentId").value(agentId));

        } finally {
            client.shutdown();
            registry.deregister(agentId);
        }
    }
}
