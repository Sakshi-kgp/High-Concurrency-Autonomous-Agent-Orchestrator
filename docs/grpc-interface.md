
# gRPC Agent Registration Interface

## 1. Purpose

This document describes the gRPC service contract and implementation for agent registration in the High-Concurrency Autonomous Agent Orchestrator.

The interface uses Protocol Buffers to define the request and response messages exchanged between an agent and the orchestrator.

The gRPC service shares the same agent registration service and registry used by the REST API, ensuring consistent agent registration across both communication interfaces.

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

The server validates registration details and registers valid agents through the shared `AgentRegistrationService`.

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

## 9. Server Configuration

The `GrpcServerConfig` class manages the gRPC server lifecycle.

- Uses Spring Boot configuration.
- Default gRPC server port is `9090`.
- Port can be configured through the `grpc.server.port` property.
- Supports dynamic port allocation for integration tests by setting the port to `0`.
- Uses `@PreDestroy` to shut down the server when the Spring application context closes.

## 10. Client Implementation

The `AgentGrpcClient` class provides client-side communication with the gRPC server.

- Creates a plaintext gRPC managed channel.
- Uses a blocking stub to invoke `RegisterAgent`.
- Applies a three-second deadline to registration requests.
- Handles communication failures through `StatusRuntimeException`.
- Returns a failure response when gRPC communication is unavailable.
- Provides a shutdown method to close the client channel.

## 11. Shared REST and gRPC Registration

Both REST and gRPC use the same `AgentRegistrationService` and underlying `AgentRegistry`.

This shared implementation provides:

- Consistent registration validation.
- Duplicate agent ID prevention.
- Common agent storage.
- Agent visibility across REST and gRPC interfaces.

An agent registered through REST can be accessed through gRPC, and an agent registered through gRPC can be retrieved through REST.

## 12. Implementation Status

### Completed

- Initial gRPC service and message definitions.
- Agent registration `.proto` file.
- Maven dependencies and Protocol Buffer code-generation configuration.
- Integration of generated Java classes.
- gRPC server and `RegisterAgent` RPC implementation.
- Configurable gRPC server port and lifecycle management.
- Shared agent registry using `ConcurrentHashMap`.
- Validation of required registration fields.
- Duplicate agent ID handling.
- gRPC client implementation.
- Client-side handling of gRPC communication failures.
- Shared REST and gRPC registration service.
- REST and gRPC cross-protocol integration tests.

## 13. Testing Results

### AgentServiceImplTest

Seven service tests validate:

- Successful registration.
- Missing agent ID.
- Missing agent name.
- Missing agent type.
- Missing agent address.
- Duplicate agent ID.
- Whitespace trimming of registration fields.

### AgentGrpcClientIntegrationTest

Four integration tests validate:

- Successful registration through the gRPC client and server.
- Handling of an unavailable gRPC server.
- Visibility of REST-registered agents through gRPC.
- Visibility of gRPC-registered agents through REST.

### Latest Project Validation

The complete Maven test suite passed successfully.

| Metric | Result |
|---|---:|
| Total tests | 44 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Build status | SUCCESS |

All 44 tests passed successfully in the latest recorded full project validation.
