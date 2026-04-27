package com.datech.mvp.integration;

import com.datech.mvp.controller.TimeEntryController;
import com.datech.mvp.dto.ProjectProfitabilityDto;
import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.service.ProjectAnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test") // h2 konfigūraciją
public class BudgetIntegrationTest {

    @Autowired
    private TimeEntryController timeEntryController;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectAnalyticsService analyticsService;

    @Test
    void addingTimeEntry_updatesBudgetStatusCorrectly() {
        // 1. Sukuriamas projektas testui
        Project project = new Project();
        project.setName("H2 Integration Test");
        project.setClientId(1L);
        project.setBudget(new BigDecimal("1000.00"));
        project.setHourlyRate(new BigDecimal("100.00"));
        project.setCurrency("EUR");
        project.setStatus("ACTIVE");
        
        // Išsaugome DB (H2 atmintyje)
        Project savedProject = projectRepository.save(project);

        // 2. Per kontrolerį pridedame 5 valandas (5 * 100 = 500 EUR)
        TimeEntry entry = new TimeEntry();
        entry.setProjectId(savedProject.getId());
        entry.setHours(new BigDecimal("5.00"));
        entry.setDate(LocalDate.now());
        timeEntryController.create(entry);

        // 3. Tikriname per Analytics servisą (profitability metodas iš jūsų kodo)
        ProjectProfitabilityDto status = analyticsService.profitability(savedProject.getId());

        // 4. Rezultatų tikrinimas
        assertNotNull(status);
        // Revenue (pajamos) turi būti 500.00
        assertEquals(0, new BigDecimal("500.00").compareTo(status.getRevenue()));
        // 500 < 1000, tad biudžetas neturi būti viršytas
        assertFalse(status.isOverBudget());
    }

    @Test
    void budgetExceeded_shouldReturnTrue() {
        Project project = new Project();
        project.setName("Overbudget H2 Test");
        project.setClientId(1L);
        project.setBudget(new BigDecimal("100.00"));
        project.setHourlyRate(new BigDecimal("150.00")); // Įkainis didesnis už biudžetą
        project = projectRepository.save(project);

        // Pridedame 1 valandą (150 EUR), viršija 100 EUR biudžetą
        TimeEntry entry = new TimeEntry();
        entry.setProjectId(project.getId());
        entry.setHours(new BigDecimal("1.00"));
        entry.setDate(LocalDate.now());
        timeEntryController.create(entry);

        // Tikriname isBudgetExceeded metodą iš jūsų kodo
        boolean isExceeded = analyticsService.isBudgetExceeded(project.getId());
        
        assertTrue(isExceeded, "Biudžetas turėtų būti viršytas");
    }
}