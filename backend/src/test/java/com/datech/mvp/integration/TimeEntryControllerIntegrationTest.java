package com.datech.mvp.integration;

import com.datech.mvp.model.Client;
import com.datech.mvp.model.Project;
import com.datech.mvp.repository.ClientRepository;
import com.datech.mvp.repository.ProjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for time entry validation (AM requirement).
 *
 * Tests the full stack: TimeEntryController → CrudService/ProjectRepository → H2 DB.
 * Covers: project existence check, archived project rejection, and field-level validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TimeEntryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Long activeProjectId;
    private Long archivedProjectId;

    @BeforeEach
    void setUp() {
        // Seed a client
        Client client = new Client();
        client.setName("Test Client");
        client.setEmail("test@example.com");
        clientRepository.save(client);

        // Seed an ACTIVE project
        Project active = new Project();
        active.setName("Active Project");
        active.setClientId(client.getId());
        active.setBudget(new BigDecimal("1000.00"));
        active.setHourlyRate(new BigDecimal("50.00"));
        active.setCurrency("EUR");
        active.setStatus("ACTIVE");
        activeProjectId = projectRepository.save(active).getId();

        // Seed an ARCHIVED project
        Project archived = new Project();
        archived.setName("Archived Project");
        archived.setClientId(client.getId());
        archived.setBudget(new BigDecimal("500.00"));
        archived.setHourlyRate(new BigDecimal("40.00"));
        archived.setCurrency("EUR");
        archived.setStatus("ARCHIVED");
        archivedProjectId = projectRepository.save(archived).getId();
    }

    // TC-AM-01: Valid time entry is saved successfully
    @Test
    void createTimeEntry_validEntry_returns200AndSavesEntry() throws Exception {
        Map<String, Object> body = Map.of(
                "projectId", activeProjectId,
                "date", LocalDate.now().toString(),
                "hours", "8.00",
                "description", "Integration test work",
                "billable", true
        );

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(activeProjectId))
                .andExpect(jsonPath("$.hours").value(8.00));
    }

    // TC-AM-02: Time entry for non-existent project is rejected
    @Test
    void createTimeEntry_nonExistentProject_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "projectId", 99999L,
                "date", LocalDate.now().toString(),
                "hours", "4.00"
        );

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // TC-AM-03: Time entry for ARCHIVED project is rejected
    @Test
    void createTimeEntry_archivedProject_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "projectId", archivedProjectId,
                "date", LocalDate.now().toString(),
                "hours", "4.00"
        );

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // TC-AM-04: Hours below minimum (0.01) are rejected by bean validation
    @Test
    void createTimeEntry_zeroHours_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "projectId", activeProjectId,
                "date", LocalDate.now().toString(),
                "hours", "0.00"
        );

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    // TC-AM-05: Hours above maximum (24.00) are rejected by bean validation
    @Test
    void createTimeEntry_tooManyHours_returns400() throws Exception {
        Map<String, Object> body = Map.of(
                "projectId", activeProjectId,
                "date", LocalDate.now().toString(),
                "hours", "25.00"
        );

        mockMvc.perform(post("/api/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
