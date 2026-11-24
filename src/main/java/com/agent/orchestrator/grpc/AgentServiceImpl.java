
package com.agent.orchestrator.grpc;

import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.service.AgentRegistrationService;
import com.orchestrator.grpc.AgentRegistrationResponse;
import com.orchestrator.grpc.AgentServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Component;

@Component
public class AgentServiceImpl extends AgentServiceGrpc.AgentServiceImplBase {

    private final AgentRegistrationService registrationService;

    public AgentServiceImpl(
            AgentRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @Override
    public void registerAgent(
            com.orchestrator.grpc.AgentRegistrationRequest request,
            StreamObserver<AgentRegistrationResponse> responseObserver) {

        String agentId = request.getAgentId();

        try {
            AgentRegistrationRequest registrationRequest =
                    new AgentRegistrationRequest(
                            request.getAgentId(),
                            request.getAgentName(),
                            request.getAgentType(),
                            request.getAgentAddress()
                    );

            boolean registered =
                    registrationService.registerAgent(registrationRequest);

            AgentRegistrationResponse response =
                    AgentRegistrationResponse.newBuilder()
                            .setSuccess(registered)
                            .setMessage(registered
                                    ? "Agent registered successfully"
                                    : "Registration failed: Agent ID already exists")
                            .setAgentId(agentId.trim())
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException exception) {

            AgentRegistrationResponse response =
                    AgentRegistrationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage(
                                    "Registration failed: "
                                            + exception.getMessage()
                            )
                            .setAgentId(agentId)
                            .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception exception) {

            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Agent registration failed")
                            .withCause(exception)
                            .asRuntimeException()
            );
        }
    }
}
