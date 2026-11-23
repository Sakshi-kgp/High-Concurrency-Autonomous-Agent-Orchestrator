
package com.agent.orchestrator.client;

import com.orchestrator.grpc.AgentRegistrationRequest;
import com.orchestrator.grpc.AgentRegistrationResponse;
import com.orchestrator.grpc.AgentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

public class AgentGrpcClient {

    private final ManagedChannel channel;
    private final AgentServiceGrpc.AgentServiceBlockingStub blockingStub;

    public AgentGrpcClient(String host, int port) {

        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();

        this.blockingStub = AgentServiceGrpc.newBlockingStub(channel);
    }

    public AgentRegistrationResponse registerAgent(
            String agentId,
            String agentName,
            String agentType,
            String agentAddress) {

        AgentRegistrationRequest request =
                AgentRegistrationRequest.newBuilder()
                        .setAgentId(agentId)
                        .setAgentName(agentName)
                        .setAgentType(agentType)
                        .setAgentAddress(agentAddress)
                        .build();

        try {
            return blockingStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .registerAgent(request);

        } catch (StatusRuntimeException exception) {

            return AgentRegistrationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage(
                            "gRPC communication failed: "
                                    + exception.getStatus().getCode()
                    )
                    .setAgentId(agentId)
                    .build();
        }
    }

    public void shutdown() {
        channel.shutdown();
    }
}
