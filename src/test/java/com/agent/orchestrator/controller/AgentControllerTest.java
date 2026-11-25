
package com.agent.orchestrator.controller;

import com.agent.orchestrator.dto.AgentRegistrationRequest;
import com.agent.orchestrator.service.AgentRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgentController.class)
class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgentRegistrationService registrationService;

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
}
