package com.agent.orchestrator.grpc;

import com.orchestrator.grpc.AgentRegistrationRequest;
import com.orchestrator.grpc.AgentRegistrationResponse;
import com.orchestrator.grpc.AgentServiceGrpc;
import io.grpc.stub.StreamObserver;

public class AgentServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    @Override
    public void registerAgent(
            AgentRegistrationRequest request,
            StreamObserver<AgentRegistrationResponse> responseObserver) {

        String agentId = request.getAgentId();
        String agentName = request.getAgentName();
        String agentType = request.getAgentType();
        String agentAddress = request.getAgentAddress();

        AgentRegistrationResponse response;

        // Validate required registration fields
        if (agentId.isBlank() ||
                agentName.isBlank() ||
                agentType.isBlank() ||
                agentAddress.isBlank()) {

            response = AgentRegistrationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Registration failed: All fields are required")
                    .setAgentId(agentId)
                    .build();

        } else {

            System.out.println("Registering agent: " + agentName);

            response = AgentRegistrationResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Agent registered successfully")
                    .setAgentId(agentId)
                    .build();
        }

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}