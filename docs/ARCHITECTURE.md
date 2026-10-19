
# Core Backend Architecture

## Project Overview

The High-Concurrency Autonomous Agent Orchestrator is a Java 21 and Spring Boot 3 backend designed to coordinate concurrent autonomous-agent workflows.

## Technology Stack

- Java 21
- Spring Boot 3
- Maven
- REST APIs
- gRPC for service communication
- Redis for distributed coordination in a later phase

## Initial Package Structure

- `com.agent.orchestrator` — application entry point
- `com.agent.orchestrator.config` — application configuration
- `com.agent.orchestrator.core` — orchestration components
- `com.agent.orchestrator.exception` — shared exception handling

## Architectural Responsibilities

The application entry point initializes the Spring application context. Configuration and orchestration logic will be kept separate so the system can evolve without coupling the core application to individual communication protocols.

## Planned Integration Points

- Virtual-thread execution configuration
- REST agent-registration API
- gRPC service definitions
- Redis-backed inter-node coordination and distributed locking

These are planned integration points and are not all implemented in the initial foundation.

## Initial Validation

- Java 21 configured in the development terminal
- Maven project configured with Spring Boot 3
- `mvn clean test` completed successfully
- Embedded Tomcat started on port 8080
