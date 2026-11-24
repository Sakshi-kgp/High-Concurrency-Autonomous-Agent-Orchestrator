
# Core Backend Architecture

## Project Overview

The High-Concurrency Autonomous Agent Orchestrator is a Java 21 and Spring Boot 3 backend designed to coordinate concurrent autonomous-agent workflows.

The application supports agent registration, retrieval, deregistration, task submission, and concurrent task execution using Java 21 Virtual Threads.

It provides REST and gRPC communication interfaces, a shared agent registry, task execution monitoring, and an interactive terminal dashboard.

## Technology Stack

- Java 21
- Spring Boot 3
- Maven
- REST APIs
- gRPC for service communication
- Java 21 Virtual Threads
- ConcurrentHashMap for agent registration
- Lanterna for interactive terminal dashboard
- JUnit 5 and Mockito for testing
- Redis for distributed coordination in a later phase

## Package Structure

- `com.agent.orchestrator` - Application entry point
- `com.agent.orchestrator.config` - Application and Virtual Thread configuration
- `com.agent.orchestrator.client` - gRPC client communication
- `com.agent.orchestrator.controller` - REST API controllers
- `com.agent.orchestrator.core` - Task execution and workflow orchestration
- `com.agent.orchestrator.dashboard` - Terminal dashboard and concurrent task metrics
- `com.agent.orchestrator.dto` - Request and response objects
- `com.agent.orchestrator.exception` - Exception handling
- `com.agent.orchestrator.grpc` - gRPC service implementation
- `com.agent.orchestrator.registry` - Agent registration and storage
- `com.agent.orchestrator.service` - Agent registration and lifecycle services

## Architectural Responsibilities

The application entry point initializes the Spring application context.

The configuration layer manages the Virtual Thread executor and gRPC server lifecycle. The registry maintains registered agents using a thread-safe data structure.

The service and core layers handle agent management, task submission, execution, timeout handling, and cancellation.

REST and gRPC components provide communication interfaces without coupling the core execution logic to a specific communication protocol.

The dashboard layer collects task execution metrics and displays the current application status through an interactive terminal interface.

## Implemented Integration Components

- Java 21 Virtual Thread execution configuration
- REST agent registration API
- Agent retrieval and deregistration
- Agent task submission through REST
- Shared agent registry
- gRPC service definitions and communication components
- Configurable gRPC server port and graceful shutdown
- Shared agent registration service across REST and gRPC
- REST and gRPC cross-protocol integration testing
- Concurrent task execution and timeout handling
- Interactive terminal dashboard using Lanterna
- Real-time task metrics: active, completed, failed, and average execution time
- Virtual Threads versus Platform Threads performance comparison

Redis-backed distributed coordination and distributed locking remain planned for a later phase.

## Initial Validation

- Java 21 configured in the development environment
- Maven project configured with Spring Boot 3
- Embedded Tomcat configured through Spring Boot
- Maven test suite executed successfully

## Virtual Thread Task Execution

The `VirtualThreadConfig` class configures a Spring-managed executor using Java 21 Virtual Threads.

### Implementation

- Uses `Executors.newVirtualThreadPerTaskExecutor()`.
- Provides an `ExecutorService` bean named `agentTaskExecutor`.
- Creates a separate Virtual Thread for each submitted task.
- Supports concurrent execution without maintaining a large fixed platform-thread pool.

The `AgentTaskExecutionService` provides a common service for submitting agent tasks to the configured `ExecutorService`.

### Task Execution Service

- Accepts tasks through Java's `Callable<T>` interface.
- Submits tasks to the Virtual Thread executor.
- Returns a `Future<T>` to retrieve the execution result.
- Rejects null task submissions.

### Concurrency Validation

The `AgentTaskExecutionServiceTests` class validates concurrent task execution.

- Submits 100 tasks to the executor.
- Uses synchronization controls to coordinate task execution.
- Tracks the maximum number of simultaneously active tasks.
- Verifies that tasks execute on Virtual Threads.
- Confirms that multiple tasks can execute concurrently.

## Task Timeout and Cancellation Handling

The `AgentTaskExecutionService` supports timed task execution through `executeTaskWithTimeout()`.

### Timeout Implementation

- Accepts a task, timeout value, and `TimeUnit`.
- Uses `Future.get(timeout, unit)` to enforce the execution time limit.
- Automatically cancels the task when a timeout occurs.
- Cancels the task if the calling thread is interrupted.
- Rejects null tasks, null time units, and non-positive timeout values.

### Cancellation Validation

- Verifies cancellation of running tasks using `Future.cancel(true)`.
- Confirms that cancelled tasks respond to interruption.
- Verifies that completed tasks cannot be cancelled.
- Tests automatic cancellation after timeout.

These behaviours are validated through automated JUnit tests.

## Simulated Agent Workload Testing

The `AgentTaskExecutionServiceTests` class validates the execution of simulated agent workloads.

### Test Configuration

- Number of simulated agents: 50
- Different task durations: 100 to 500 milliseconds
- Tasks submitted through `AgentTaskExecutionService`
- Results collected using `Future` objects

### Test Validation

- Verifies successful completion of all 50 tasks.
- Confirms that each task returns its expected agent identifier.
- Uses the configured Java 21 Virtual Thread executor.

## Performance Testing

The `AgentTaskPerformanceTests` class evaluates concurrent execution under different workload sizes.

### Test Configuration

The benchmark evaluates the following workload sizes:

- 50 tasks
- 100 tasks
- 250 tasks
- 500 tasks

Each task simulates approximately 50 milliseconds of work.

### Performance Metrics

The test measures:

- Total execution time
- Throughput in tasks per second
- Average task execution latency
- Peak number of simultaneously active tasks
- Approximate JVM heap memory change

### Sample Performance Results

One observed benchmark run comparing Virtual Threads and Platform Threads for 500 simulated I/O-bound tasks:

| Metric | Virtual Threads | Platform Threads |
|---|---:|---:|
| Tasks submitted | 500 | 500 |
| Execution time | 55 ms | 811 ms |
| Throughput | 8,966.78 tasks/sec | 616.15 tasks/sec |
| Average latency | 52.47 ms | 418.41 ms |
| Peak concurrency | 500 | 32 |
| Approximate heap change | 1 MB | 2 MB |

Virtual Threads achieved approximately 14.55 times the throughput in this particular workload.

These values represent one benchmark run and may vary across executions and hardware configurations.

### Benchmark Limitations

- The benchmark uses simulated I/O-bound workloads rather than real agent operations.
- Memory change is an approximate JVM heap snapshot, not total process memory usage.
- Peak concurrency represents active submitted tasks, not the total number of JVM Virtual Threads.
- Results may vary depending on hardware and JVM conditions.
- Results are intended for basic performance evaluation, not formal benchmarking.

## Agent Registration and Management

The `AgentRegistry` maintains registered agents using `ConcurrentHashMap`.

### Registry Operations

- Registers agents using `putIfAbsent()` to prevent duplicate identifiers.
- Retrieves agents using their unique agent ID.
- Returns a list of all registered agents.
- Provides the current number of registered agents.
- Supports agent deregistration.

The `AgentRegistrationService` validates agent details and manages registration, retrieval, and deregistration.

### REST Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/agents/register` | Register a new agent |
| GET | `/api/agents/{agentId}` | Retrieve an agent |
| GET | `/api/agents` | Retrieve all agents |
| DELETE | `/api/agents/{agentId}` | Deregister an agent |
| POST | `/api/agents/{agentId}/tasks` | Submit a task for an agent |

### Registration Validation

- Rejects missing or blank agent details.
- Trims whitespace from registration fields.
- Returns HTTP 201 CREATED for successful registration.
- Returns HTTP 409 CONFLICT for duplicate agent IDs.
- Returns HTTP 400 BAD REQUEST for invalid registration requests.

## gRPC Communication

The application provides gRPC-based agent registration through the `AgentServiceImpl` service implementation.

### gRPC Components

- `AgentServiceImpl` handles incoming agent registration requests.
- `AgentGrpcClient` provides client-side communication.
- `GrpcServerConfig` manages the gRPC server lifecycle.
- The gRPC server uses a configurable port with a default value of 9090.
- The server is shut down through Spring's `@PreDestroy` lifecycle hook.

### Shared Registration

REST and gRPC use the same `AgentRegistrationService` and `AgentRegistry`.

This ensures that agents registered through either communication interface are available through the other interface.

### Integration Testing

The integration tests verify:

- Agent registration through the gRPC server.
- Graceful handling of an unavailable gRPC server.
- Visibility of REST-registered agents through gRPC.
- Visibility of gRPC-registered agents through REST.

## Agent Task Workflow Integration

The `AgentTaskWorkflowService` connects agent registration with task execution.

### Workflow Implementation

1. Receives an agent ID and a task.
2. Validates the task.
3. Retrieves the requested agent from `AgentRegistrationService`.
4. Rejects requests for unregistered agents.
5. Submits valid tasks through `AgentTaskExecutionService`.
6. Returns a `Future<T>` representing the submitted task.

This connects the agent registry with the Java 21 Virtual Thread execution infrastructure.

### REST Task Execution

The task execution endpoint accepts a task name and execution duration.

Example request:

```json
{
  "taskName": "Data Processing",
  "durationMillis": 1000
}
```

The endpoint:

- Validates the task name and duration.
- Checks that the requested agent is registered.
- Submits the simulated workload through the workflow service.
- Executes the workload using the Virtual Thread executor.
- Waits for the task result.
- Returns the execution result or an appropriate HTTP error response.

The endpoint supports task execution through a REST interface while retaining the asynchronous `Future`-based execution mechanism internally.

## Dashboard and Task Monitoring

The dashboard module provides an interactive terminal interface using the Lanterna library.

### Dashboard Implementation

The `TerminalDashboard` component implements Spring Boot's `ApplicationRunner` interface.

- Dashboard activation is controlled through the `dashboard.enabled` property.
- The dashboard runs on a Java 21 Virtual Thread.
- The screen refreshes every second.
- Pressing `Q` exits the dashboard screen.

### Dashboard Metrics

The `DashboardMetricsService` maintains thread-safe execution metrics.

- Number of active tasks.
- Number of completed tasks.
- Number of failed tasks.
- Total number of executed tasks.
- Average task execution time in milliseconds.

`AtomicLong` is used for active task tracking, while `LongAdder` is used for concurrent completed-task, failed-task, and execution-time updates.

The `AgentTaskWorkflowService` updates these metrics during task execution.

## REST API Testing

The `AgentControllerTest` class validates REST API behaviour using Spring MockMvc and Mockito.

The tests cover:

- Successful agent registration.
- Duplicate agent registration.
- Invalid registration requests.
- Successful agent retrieval.
- Unknown agent retrieval.
- Retrieving all registered agents.
- Empty agent lists.
- Successful deregistration.
- Deregistration of unknown agents.
- Successful task execution.
- Task submission for unregistered agents.
- Invalid task requests.
- Task execution failures.

## Concurrency Limitations

- Virtual Threads do not eliminate CPU, memory, or external resource constraints.
- Blocking operations can still affect overall application performance.
- Task cancellation depends on tasks responding appropriately to interruption.
- Timeout cancellation requests interruption but cannot guarantee termination of tasks that ignore interruption.
- The current agent workload uses simulated tasks rather than production agent operations.
- Redis-based distributed coordination is not yet implemented.
- The performance benchmark uses a simulated workload and is not a formal production benchmark.

## Final Validation

The complete Maven test suite was executed successfully after REST, gRPC, Virtual Thread, dashboard, and workflow integration.

**Latest validation result:**

| Metric | Result |
|---|---:|
| Total tests | 44 |
| Failures | 0 |
| Errors | 0 |
| Skipped | 0 |
| Build status | SUCCESS |

All 44 tests passed successfully, covering Virtual Thread execution, concurrency, timeout and cancellation handling, performance evaluation, agent registration, REST operations, gRPC communication, cross-protocol integration, and task workflow integration.
