# gRPC Agent Registration Interface

## 1. Purpose

This document describes the gRPC service contract and implementation for agent registration in the High-Concurrency Autonomous Agent Orchestrator.

The interface uses Protocol Buffers to define the request and response messages exchanged between an agent and the orchestrator.

## 2. Protocol Definition

**File:** `src/main/proto/agent_service.proto`

- Protocol syntax: `proto3`
- Java package: `com.orchestrator.grpc`
- Service name: `AgentService`

The Protocol Buffer Maven plugin generates the Java classes used by the gRPC client and server.

## 3. Service Definition

### RegisterAgent

`RegisterAgent` is a unary RPC: the client sends one request and receives one response.

**Request:** `AgentRegistrationRequest`

**Response:** `AgentRegistrationResponse`

The server validates registration details and registers valid agents in the shared agent registry.

## 4. Request Message

The `AgentRegistrationRequest` message contains:

| Field | Type | Purpose |
|---|---|---|
| `agent_id` | string | Identifies the agent |
| `agent_name` | string | Human-readable agent name |
| `agent_type` | string | Describes the agent category |
| `agent_address` | string | Specifies the agent's network address |

## 5. Response Message

The `AgentRegistrationResponse` message contains:

| Field | Type | Purpose |
|---|---|---|
| `success` | bool | Indicates whether registration succeeded |
| `message` | string | Describes the outcome |
| `agent_id` | string | Identifies the agent |

## 6. Example Request

The following JSON illustrates the logical contents of a registration request:

```json
{
  "agent_id": "agent-001",
  "agent_name": "ResearchAgent",
  "agent_type": "research",
  "agent_address": "localhost:50051"
}
```

## 7. Example Response

A successful registration could return:

```json
{
  "success": true,
  "message": "Agent registered successfully",
  "agent_id": "agent-001"
}
```

These examples illustrate the message contents.

## 8. Maven Dependencies and Configuration

The following dependencies are configured for gRPC integration:

| Dependency | Purpose |
|---|---|
| `grpc-netty-shaded` | gRPC network transport |
| `grpc-protobuf` | Protocol Buffers integration |
| `grpc-stub` | gRPC client and server stub APIs |
| `protobuf-java` | Protocol Buffers Java runtime |
| `javax.annotation-api` | Supports annotations used by generated code |

Configured versions:

- gRPC: `1.68.2`
- Protocol Buffers: `3.25.5`
- Java target: `21`
- Spring Boot: `3.5.6`

The Maven Protocol Buffer plugin configuration is implemented and used to generate Java classes from the `.proto` definition.

## 9. Implementation Status

### Completed

- Initial gRPC service and message definitions.
- Agent registration `.proto` file.
- Maven dependencies and Protocol Buffer code-generation configuration.
- Integration of generated Java classes.
- gRPC server and `RegisterAgent` RPC implementation.
- Shared agent registry using `ConcurrentHashMap`.
- Validation of required registration fields.
- Duplicate agent ID handling.
- gRPC client implementation.
- Client-side handling of gRPC communication failures.
- Client-server integration tests.

### Testing Results

The following tests have passed:

- gRPC server registration unit tests.
- Successful registration through the gRPC client and server.
- Handling of an unavailable gRPC server.

### Pending

- Coordination with the team regarding the REST agent-registration endpoint.
- REST and gRPC registration compatibility testing.
- Documentation of the compatibility test results once the REST endpoint is available.