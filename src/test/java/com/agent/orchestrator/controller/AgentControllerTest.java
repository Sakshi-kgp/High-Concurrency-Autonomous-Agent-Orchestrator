
package com.agent.orchestrator.controller;

import com.agent.orchestrator.core.AgentTaskWorkflowService;
import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.registry.RegisteredAgent;
import com.agent.orchestrator.service.AgentRegistrationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentRegistrationService registrationService;

    @MockBean
    private AgentTaskWorkflowService workflowService;

    @Test
    void shouldRegisterAgentSuccessfully() throws Exception {
        when(registrationService.registerAgent(
                new AgentRegistrationRequest(
                        "agent-101",
                        "Research Agent",
                        "AI",
                        "localhost:50051"
                )
        )).thenReturn(true);

        mockMvc.perform(post("/api/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agentId": "agent-101",
                                    "agentName": "Research Agent",
                                    "agentType": "AI",
                                    "agentAddress": "localhost:50051"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Agent registered successfully"))
                .andExpect(jsonPath("$.agentId").value("agent-101"));
    }

    @Test
    void shouldReturnConflictForDuplicateAgent() throws Exception {
        when(registrationService.registerAgent(
                new AgentRegistrationRequest(
                        "agent-101",
                        "Research Agent",
                        "AI",
                        "localhost:50051"
                )
        )).thenReturn(false);

        mockMvc.perform(post("/api/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agentId": "agent-101",
                                    "agentName": "Research Agent",
                                    "agentType": "AI",
                                    "agentAddress": "localhost:50051"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Agent ID already exists"))
                .andExpect(jsonPath("$.agentId").value("agent-101"));
    }

    @Test
    void shouldReturnBadRequestForInvalidAgent() throws Exception {
        when(registrationService.registerAgent(
                new AgentRegistrationRequest(
                        "",
                        "Research Agent",
                        "AI",
                        "localhost:50051"
                )
        )).thenThrow(new IllegalArgumentException("All fields are required"));

        mockMvc.perform(post("/api/agents/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "agentId": "",
                                    "agentName": "Research Agent",
                                    "agentType": "AI",
                                    "agentAddress": "localhost:50051"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("All fields are required"))
                .andExpect(jsonPath("$.agentId").value(""));
    }

    @Test
    void shouldRetrieveAgentSuccessfully() throws Exception {
        RegisteredAgent agent = new RegisteredAgent(
                "agent-101",
                "Research Agent",
                "AI",
                "localhost:50051"
        );

        when(registrationService.getAgent("agent-101"))
                .thenReturn(Optional.of(agent));

        mockMvc.perform(get("/api/agents/agent-101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.agentId").value("agent-101"))
                .andExpect(jsonPath("$.agentName").value("Research Agent"))
                .andExpect(jsonPath("$.agentType").value("AI"))
                .andExpect(jsonPath("$.agentAddress")
                        .value("localhost:50051"));
    }

    @Test
    void shouldReturnNotFoundForUnknownAgent() throws Exception {
        when(registrationService.getAgent("unknown"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/agents/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRetrieveAllAgents() throws Exception {
        RegisteredAgent first = new RegisteredAgent(
                "agent-101",
                "Research Agent",
                "AI",
                "localhost:50051"
        );

        RegisteredAgent second = new RegisteredAgent(
                "agent-102",
                "Data Agent",
                "ML",
                "localhost:50052"
        );

        when(registrationService.getAllAgents())
                .thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].agentId").value("agent-101"))
                .andExpect(jsonPath("$[1].agentId").value("agent-102"));
    }

    @Test
    void shouldReturnEmptyListWhenNoAgentsExist() throws Exception {
        when(registrationService.getAllAgents())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldDeregisterAgentSuccessfully() throws Exception {
        when(registrationService.deregisterAgent("agent-101"))
                .thenReturn(true);

        mockMvc.perform(delete("/api/agents/agent-101"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeregisteringUnknownAgent()
            throws Exception {

        when(registrationService.deregisterAgent("unknown"))
                .thenReturn(false);

        mockMvc.perform(delete("/api/agents/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldExecuteTaskForRegisteredAgent() throws Exception {
        doReturn(CompletableFuture.completedFuture("Task completed"))
                .when(workflowService)
                .submitAgentTask(eq("agent-101"), any(Callable.class));

        mockMvc.perform(post("/api/agents/agent-101/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": "Data Processing",
                                    "durationMillis": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.agentId").value("agent-101"))
                .andExpect(jsonPath("$.taskName").value("Data Processing"))
                .andExpect(jsonPath("$.result").value("Task completed"));
    }

    @Test
    void shouldRejectTaskForUnregisteredAgent() throws Exception {
        when(workflowService.submitAgentTask(
                eq("unknown"),
                any(Callable.class)
        )).thenThrow(new IllegalArgumentException("Agent not found: unknown"));

        mockMvc.perform(post("/api/agents/unknown/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": "Data Processing",
                                    "durationMillis": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Agent not found: unknown"));
    }

    @Test
    void shouldRejectInvalidTaskRequest() throws Exception {
        mockMvc.perform(post("/api/agents/agent-101/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": "",
                                    "durationMillis": 100
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnInternalServerErrorWhenTaskFails()
            throws Exception {

        doReturn(CompletableFuture.failedFuture(
                new IllegalStateException("Task execution failed")
        )).when(workflowService)
                .submitAgentTask(eq("agent-101"), any(Callable.class));

        mockMvc.perform(post("/api/agents/agent-101/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "taskName": "Data Processing",
                                    "durationMillis": 100
                                }
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message")
                        .value("Task execution failed"));
    }
}
