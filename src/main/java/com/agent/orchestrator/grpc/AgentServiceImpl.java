package com.agent.orchestrator.grpc;

import com.agent.orchestrator.registry.AgentRegistry;
import com.agent.orchestrator.registry.RegisteredAgent;
import com.orchestrator.grpc.AgentRegistrationRequest;
import com.orchestrator.grpc.AgentRegistrationResponse;
import com.orchestrator.grpc.AgentServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class AgentServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    private final AgentRegistry registry;

    public AgentServiceImpl(AgentRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void registerAgent(
            AgentRegistrationRequest request,
            StreamObserver<AgentRegistrationResponse> responseObserver) {

        String agentId = request.getAgentId();
        String agentName = request.getAgentName();
        String agentType = request.getAgentType();
        String agentAddress = request.getAgentAddress();

        AgentRegistrationResponse response;

        if (agentId.isBlank()
                || agentName.isBlank()
                || agentType.isBlank()
                || agentAddress.isBlank()) {

            response = AgentRegistrationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Registration failed: All fields are required")
                    .setAgentId(agentId)
                    .build();

        } else {
            RegisteredAgent agent = new RegisteredAgent(
                    agentId,
                    agentName,
                    agentType,
                    agentAddress
            );

            boolean registered = registry.register(agent);

            if (registered) {
                response = AgentRegistrationResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Agent registered successfully")
                        .setAgentId(agentId)
                        .build();
            } else {
                response = AgentRegistrationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Registration failed: Agent ID already exists")
                        .setAgentId(agentId)
                        .build();
            }
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}