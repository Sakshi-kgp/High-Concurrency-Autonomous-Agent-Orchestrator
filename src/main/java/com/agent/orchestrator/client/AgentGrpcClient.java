package com.agent.orchestrator.client;

import com.orchestrator.grpc.AgentRegistrationRequest;
import com.orchestrator.grpc.AgentRegistrationResponse;
import com.orchestrator.grpc.AgentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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

        return blockingStub.registerAgent(request);
    }

    public void shutdown() {
        channel.shutdown();
    }
}