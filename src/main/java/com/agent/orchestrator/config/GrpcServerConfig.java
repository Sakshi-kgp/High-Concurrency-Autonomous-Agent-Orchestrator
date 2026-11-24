
package com.agent.orchestrator.config;

import com.agent.orchestrator.grpc.AgentServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    private Server grpcServer;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Bean
    public Server grpcServer(AgentServiceImpl agentService) throws IOException {

        grpcServer = ServerBuilder
                .forPort(grpcPort)
                .addService(agentService)
                .build()
                .start();

        System.out.println(
                "gRPC server started on port " + grpcServer.getPort()
        );

        return grpcServer;
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }
}
