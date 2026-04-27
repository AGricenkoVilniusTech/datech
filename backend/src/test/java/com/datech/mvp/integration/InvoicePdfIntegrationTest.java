package com.datech.mvp.integration;

import com.datech.mvp.model.Client;
import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.ClientRepository;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import com.datech.mvp.service.InvoicePdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for invoice PDF generation (AM requirement).
 *
 * Tests the full stack: InvoicePdfService → ClientRepository + ProjectRepository
 * + TimeEntryRepository → H2 DB. Verifies that cross-component data retrieval
 * and calculation produce a valid PDF output.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class InvoicePdfIntegrationTest {

    @Autowired
    private InvoicePdfService invoicePdfService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    private Long clientId;

    @BeforeEach
    void setUp() {
        // Seed client
        Client client = new Client();
        client.setName("PDF Test Client");
        client.setEmail("pdf@example.com");
        clientId = clientRepository.save(client).getId();

        // Seed project linked to client
        Project project = new Project();
        project.setName("PDF Test Project");
        project.setClientId(clientId);
        project.setBudget(new BigDecimal("2000.00"));
        project.setHourlyRate(new BigDecimal("60.00"));
        project.setCurrency("EUR");
        project.setStatus("ACTIVE");
        Long projectId = projectRepository.save(project).getId();

        // Seed time entry within date range
        TimeEntry entry = new TimeEntry();
        entry.setProjectId(projectId);
        entry.setDate(LocalDate.of(2026, 3, 15));
        entry.setHours(new BigDecimal("5.00"));
        entry.setDescription("Test work");
        entry.setBillable(true);
        timeEntryRepository.save(entry);
    }

    // TC-AM-06: PDF is generated for a valid client and date range
    @Test
    void generatePdf_validClientAndDateRange_returnsPdfBytes() {
        byte[] pdf = invoicePdfService.generateInvoicePdf(
                clientId,
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );

        assertNotNull(pdf);
        assertTrue(pdf.length > 0, "PDF should not be empty");
        // PDF files start with the %PDF magic bytes
        assertEquals('%', (char) pdf[0]);
        assertEquals('P', (char) pdf[1]);
        assertEquals('D', (char) pdf[2]);
        assertEquals('F', (char) pdf[3]);
    }

    // TC-AM-07: Start date after end date is rejected before reaching repository
    @Test
    void generatePdf_startDateAfterEndDate_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                invoicePdfService.generateInvoicePdf(
                        clientId,
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 3, 1)
                )
        );

        assertTrue(ex.getMessage().contains("Start date must be before"));
    }

    // TC-AM-08: Date range exceeding 365 days is rejected
    @Test
    void generatePdf_dateRangeOver365Days_throwsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                invoicePdfService.generateInvoicePdf(
                        clientId,
                        LocalDate.of(2025, 1, 1),
                        LocalDate.of(2026, 6, 1)
                )
        );

        assertTrue(ex.getMessage().contains("365 days"));
    }

    // TC-AM-09: Non-existent client throws IllegalArgumentException
    @Test
    void generatePdf_nonExistentClient_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                invoicePdfService.generateInvoicePdf(
                        99999L,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 3, 31)
                )
        );
    }
}
