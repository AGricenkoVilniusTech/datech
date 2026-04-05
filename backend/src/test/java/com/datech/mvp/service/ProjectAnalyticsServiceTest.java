package com.datech.mvp.service;

import com.datech.mvp.dto.ProjectProfitabilityDto;
import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectAnalyticsServiceTest {

    private ProjectRepository projectRepository;
    private TimeEntryRepository timeEntryRepository;
    private InvoiceRepository invoiceRepository;
    private ProjectAnalyticsService service;

    @BeforeEach
    void setUp() {
        projectRepository = Mockito.mock(ProjectRepository.class);
        timeEntryRepository = Mockito.mock(TimeEntryRepository.class);
        invoiceRepository = Mockito.mock(InvoiceRepository.class);
        service = new ProjectAnalyticsService(projectRepository, timeEntryRepository, invoiceRepository);
    }

    private Project makeProject(Long id, String hourlyRate, String budget) {
        Project p = new Project();
        p.setId(id);
        p.setName("Test Project");
        p.setClientId(1L);
        p.setHourlyRate(new BigDecimal(hourlyRate));
        p.setBudget(new BigDecimal(budget));
        return p;
    }

    private TimeEntry makeEntry(BigDecimal hours) {
        TimeEntry e = new TimeEntry();
        e.setHours(hours);
        return e;
    }

    // FR-1: basic calculation 8h x 50 = 400.00
    @Test
    void profitability_basicCalculation() {
        Project p = makeProject(1L, "50.00", "300.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(makeEntry(new BigDecimal("8.00")))
        );

        ProjectProfitabilityDto result = service.profitability(1L);

        assertEquals(new BigDecimal("400.00"), result.getRevenue());
    }

    // FR-1: result is rounded to 2 decimal places
    @Test
    void profitability_revenueRoundedToTwoDecimals() {
        Project p = makeProject(1L, "33.33", "100.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(makeEntry(new BigDecimal("3.00")))
        );

        ProjectProfitabilityDto result = service.profitability(1L);

        assertEquals(2, result.getRevenue().scale());
    }

    // FR-1: multiple entries summed correctly
    @Test
    void profitability_multipleEntriesSummed() {
        Project p = makeProject(1L, "50.00", "100.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(
                        makeEntry(new BigDecimal("2.00")),
                        makeEntry(new BigDecimal("3.00")),
                        makeEntry(new BigDecimal("3.00"))
                )
        );

        ProjectProfitabilityDto result = service.profitability(1L);

        assertEquals(new BigDecimal("400.00"), result.getRevenue());
    }

    // FR-1: zero entries = 0.00 revenue
    @Test
    void profitability_noEntries_revenueIsZero() {
        Project p = makeProject(1L, "50.00", "100.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(List.of());

        ProjectProfitabilityDto result = service.profitability(1L);

        assertEquals(0, result.getRevenue().compareTo(BigDecimal.ZERO));
    }

    // FR-1: hourly rate 0.00 = total 0.00
    @Test
    void profitability_zeroRate_revenueIsZero() {
        Project p = makeProject(1L, "0.00", "100.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(makeEntry(new BigDecimal("8.00")))
        );

        ProjectProfitabilityDto result = service.profitability(1L);

        assertEquals(0, result.getRevenue().compareTo(BigDecimal.ZERO));
    }

    // overBudget is true when revenue exceeds budget
    @Test
    void profitability_overBudget_true() {
        Project p = makeProject(1L, "50.00", "300.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(makeEntry(new BigDecimal("8.00")))
        );

        assertTrue(service.profitability(1L).isOverBudget());
    }

    // overBudget is false when revenue is under budget
    @Test
    void profitability_overBudget_false() {
        Project p = makeProject(1L, "50.00", "500.00");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(p));
        when(timeEntryRepository.findByProjectId(1L)).thenReturn(
                List.of(makeEntry(new BigDecimal("8.00")))
        );

        assertFalse(service.profitability(1L).isOverBudget());
    }

    // project not found throws exception
    @Test
    void profitability_projectNotFound_throwsException() {
        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.profitability(99L));
    }
}