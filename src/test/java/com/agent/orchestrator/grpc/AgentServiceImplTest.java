
package com.agent.orchestrator.grpc;

import com.agent.orchestrator.registry.AgentRegistry;
import com.orchestrator.grpc.AgentRegistrationRequest;
import com.orchestrator.grpc.AgentRegistrationResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentServiceImplTest {

    private final AgentRegistry registry = new AgentRegistry();
    private final AgentServiceImpl service = new AgentServiceImpl(registry);

    private AgentRegistrationResponse register(
            AgentRegistrationRequest request) {

        List<AgentRegistrationResponse> responses = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();

        service.registerAgent(
                request,
                new StreamObserver<AgentRegistrationResponse>() {

                    @Override
                    public void onNext(AgentRegistrationResponse response) {
                        responses.add(response);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        errors.add(throwable);
                    }

                    @Override
                    public void onCompleted() {
                        // Registration completed
                    }
                }
        );

        assertTrue(errors.isEmpty(), "RPC should not return an error");
        assertEquals(1, responses.size(), "RPC should return one response");

        return responses.get(0);
    }

    @Test
    void registerAgentWithValidDetailsShouldSucceed() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("agent-001")
                        .setAgentName("ResearchAgent")
                        .setAgentType("research")
                        .setAgentAddress("localhost:50051")
                        .build();

        AgentRegistrationResponse response = register(request);

        assertTrue(response.getSuccess());
        assertEquals("Agent registered successfully", response.getMessage());
        assertEquals("agent-001", response.getAgentId());
    }

    @Test
    void registerAgentWithMissingIdShouldFail() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("")
                        .setAgentName("ResearchAgent")
                        .setAgentType("research")
                        .setAgentAddress("localhost:50051")
                        .build();

        AgentRegistrationResponse response = register(request);

        assertFalse(response.getSuccess());
        assertEquals(
                "Registration failed: All fields are required",
                response.getMessage()
        );
    }

    @Test
    void registerAgentWithMissingNameShouldFail() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("agent-002")
                        .setAgentName("")
                        .setAgentType("research")
                        .setAgentAddress("localhost:50051")
                        .build();

        AgentRegistrationResponse response = register(request);

        assertFalse(response.getSuccess());
        assertEquals(
                "Registration failed: All fields are required",
                response.getMessage()
        );
    }

    @Test
    void registerAgentWithMissingTypeShouldFail() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("agent-003")
                        .setAgentName("ResearchAgent")
                        .setAgentType("")
                        .setAgentAddress("localhost:50051")
                        .build();

        AgentRegistrationResponse response = register(request);

        assertFalse(response.getSuccess());
        assertEquals(
                "Registration failed: All fields are required",
                response.getMessage()
        );
    }

    @Test
    void registerAgentWithMissingAddressShouldFail() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("agent-004")
                        .setAgentName("ResearchAgent")
                        .setAgentType("research")
                        .setAgentAddress("")
                        .build();

        AgentRegistrationResponse response = register(request);

        assertFalse(response.getSuccess());
        assertEquals(
                "Registration failed: All fields are required",
                response.getMessage()
        );
    }

    @Test
    void registerAgentWithDuplicateIdShouldFail() {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId("agent-duplicate")
                        .setAgentName("ResearchAgent")
                        .setAgentType("research")
                        .setAgentAddress("localhost:50051")
                        .build();

        AgentRegistrationResponse firstResponse = register(request);
        AgentRegistrationResponse secondResponse = register(request);

        assertTrue(firstResponse.getSuccess());
        assertFalse(secondResponse.getSuccess());

        assertEquals(
                "Registration failed: Agent ID already exists",
                secondResponse.getMessage()
        );

        assertEquals(1, registry.count());
    }
}
