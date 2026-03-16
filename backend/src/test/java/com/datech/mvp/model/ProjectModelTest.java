package com.datech.mvp.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ProjectModelTest {

    @Test
    void shouldSetAndGetAllFields() {
        Project p = new Project();
        p.setId(1L);
        p.setName("Test Project");
        p.setClientId(2L);
        p.setBudget(new BigDecimal("1000.00"));
        p.setHourlyRate(new BigDecimal("50.00"));
        p.setCurrency("EUR");
        p.setStatus("ACTIVE");
        p.setStartDate(LocalDate.of(2026, 1, 1));
        p.setEndDate(LocalDate.of(2026, 12, 31));

        assertEquals(1L, p.getId());
        assertEquals("Test Project", p.getName());
        assertEquals(2L, p.getClientId());
        assertEquals(new BigDecimal("1000.00"), p.getBudget());
        assertEquals(new BigDecimal("50.00"), p.getHourlyRate());
        assertEquals("EUR", p.getCurrency());
        assertEquals("ACTIVE", p.getStatus());
        assertEquals(LocalDate.of(2026, 1, 1), p.getStartDate());
        assertEquals(LocalDate.of(2026, 12, 31), p.getEndDate());
    }

    @Test
    void shouldHaveDefaultCurrencyEUR() {
        Project p = new Project();
        assertEquals("EUR", p.getCurrency());
    }

    @Test
    void shouldHaveDefaultStatusACTIVE() {
        Project p = new Project();
        assertEquals("ACTIVE", p.getStatus());
    }
}