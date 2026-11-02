# gRPC Agent Registration Interface

## 1. Purpose

This document describes the initial gRPC service contract for agent registration in the High-Concurrency Autonomous Agent Orchestrator.

The interface uses Protocol Buffers to define the request and response messages exchanged between an agent and the orchestrator.

## 2. Protocol Definition

**File:** `src/main/proto/agent_service.proto`

- Protocol syntax: `proto3`
- Java package: `com.orchestrator.grpc`
- Service name: `AgentService`

## 3. Service Definition

### RegisterAgent

`RegisterAgent` is a unary RPC: the client sends one request and receives one response.

**Request:** `AgentRegistrationRequest`

**Response:** `AgentRegistrationResponse`

The RPC defines the contract for submitting agent registration information. The actual server-side registration logic will be implemented separately.

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
| `agent_id` | string | Identifies the registered agent |

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

These are illustrative examples, not proof that registration has been implemented or tested.

## 8. Maven Dependencies

The following dependencies have been added to `pom.xml` locally for gRPC integration:

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

The project targets Java 21.

## 9. Implementation Status

- Initial gRPC service and message definitions: prepared.
- Agent registration `.proto` file: created.
- Required Maven dependencies: added locally and dependency resolution succeeded.
- Protobuf Maven code-generation configuration: pending.
- Generated Java classes: pending verification.
- gRPC server and registration logic: Done.
- End-to-end gRPC testing: Done