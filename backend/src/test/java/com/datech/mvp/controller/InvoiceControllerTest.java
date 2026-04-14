package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.InvoiceReminderService;
import com.datech.mvp.service.ProjectAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceControllerTest {

    @Mock private InvoiceRepository repository;
    @Mock private CrudService crudService;
    @Mock private ProjectAnalyticsService analyticsService;
    @Mock private InvoiceReminderService reminderService;
    @Mock private TaxCalculator taxCalculator;
    @Mock private InvoicePdfService invoicePdfService;

    @InjectMocks
    private InvoiceController controller;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setDueDate(LocalDate.now().plusDays(5));
        invoice.setRemind3DaysBefore(true);
        invoice.setRemind1DayBefore(false);
        invoice.setRemindOnDueDate(false);
    }

    @Test
    void create_shouldSaveAndCreateReminders() {
        when(taxCalculator.calculateTax(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.ZERO);
        when(taxCalculator.calculateTotal(any(BigDecimal.class), any(BigDecimal.class))).thenReturn(BigDecimal.TEN);
        when(crudService.save(repository, invoice)).thenReturn(invoice);

        Invoice result = controller.create(invoice);

        assertSame(invoice, result);
        verify(crudService).save(repository, invoice);
        verify(reminderService).createRemindersFromInvoice(invoice);
    }

    @Test
    void create_shouldRejectNullDueDate() {
        invoice.setDueDate(null);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.create(invoice));

        assertTrue(ex.getReason().contains("Due date is required"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void create_shouldRejectPastDueDate() {
        invoice.setDueDate(LocalDate.now().minusDays(1));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.create(invoice));

        assertTrue(ex.getReason().contains("cannot be in the past"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void create_shouldRejectIfNoReminderSelected() {
        invoice.setRemind3DaysBefore(false);
        invoice.setRemind1DayBefore(false);
        invoice.setRemindOnDueDate(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.create(invoice));

        assertTrue(ex.getReason().contains("At least one reminder"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void update_shouldCancelRemindersWhenPaid() {
        invoice.setStatus("PAID");
        when(crudService.save(repository, invoice)).thenReturn(invoice);

        controller.update(1L, invoice);

        verify(reminderService).cancelScheduledReminders(1L);
    }

    @Test
    void update_shouldNotCancelRemindersWhenNotPaid() {
        invoice.setStatus("SENT");
        when(crudService.save(repository, invoice)).thenReturn(invoice);

        controller.update(1L, invoice);

        verify(reminderService, never()).cancelScheduledReminders(any());
    }

    @Test
    void all_shouldReturnAllInvoices() {
        when(crudService.findAll(repository)).thenReturn(List.of(invoice));

        List<Invoice> result = controller.all();

        assertEquals(1, result.size());
    }

    @Test
    void delete_shouldCallCrudService() {
        controller.delete(1L);
        verify(crudService).delete(repository, 1L);
    }
}