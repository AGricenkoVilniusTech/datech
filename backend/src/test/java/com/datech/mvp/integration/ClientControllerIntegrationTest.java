package com.datech.mvp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.datech.mvp.model.Client;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ClientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateClientSuccessfully() throws Exception {
        Client client = new Client();
        client.setName("Integration Client " + System.currentTimeMillis());
        client.setEmail("integration@test.com");
        client.setCompany("Test Company");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("integration@test.com"));
    }

    @Test
    void shouldFailWhenNameIsEmpty() throws Exception {
        Client client = new Client();
        client.setName("");
        client.setEmail("test@test.com");

        mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isBadRequest());
    }
}