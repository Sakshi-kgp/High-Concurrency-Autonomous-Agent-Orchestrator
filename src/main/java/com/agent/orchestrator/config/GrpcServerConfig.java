package com.agent.orchestrator.config;

import com.agent.orchestrator.grpc.AgentServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GrpcServerConfig {

    private Server grpcServer;

    @Bean
    public Server grpcServer() throws IOException {

        grpcServer = ServerBuilder
                .forPort(9090)
                .addService(new AgentServiceImpl())
                .build()
                .start();

        System.out.println("gRPC server started on port 9090");

        return grpcServer;
    }

    @PreDestroy
    public void stopGrpcServer() {

        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    }
}