package com.datech.mvp.service;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.model.InvoiceReminder;
import com.datech.mvp.repository.InvoiceReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceReminderServiceTest {

    @Mock
    private InvoiceReminderRepository reminderRepository;

    @InjectMocks
    private InvoiceReminderService reminderService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setProjectId(10L);
        invoice.setIssueDate(LocalDate.of(2026, 4, 1));
        invoice.setDueDate(LocalDate.of(2026, 4, 10));
        invoice.setAmount(new BigDecimal("100.00"));
        invoice.setStatus("UNPAID");
    }

    @Test
    void createDefaultReminders_shouldCreateThreeReminders() {
        reminderService.createDefaultReminders(invoice);

        ArgumentCaptor<InvoiceReminder> captor = ArgumentCaptor.forClass(InvoiceReminder.class);
        verify(reminderRepository, times(3)).save(captor.capture());

        List<InvoiceReminder> saved = captor.getAllValues();
        assertEquals(3, saved.size());

        assertEquals(LocalDate.of(2026, 4, 7), saved.get(0).getRemindAt());
        assertEquals("DUE_MINUS_3", saved.get(0).getType());
        assertEquals("SCHEDULED", saved.get(0).getStatus());

        assertEquals(LocalDate.of(2026, 4, 9), saved.get(1).getRemindAt());
        assertEquals("DUE_MINUS_1", saved.get(1).getType());

        assertEquals(LocalDate.of(2026, 4, 10), saved.get(2).getRemindAt());
        assertEquals("DUE_TODAY", saved.get(2).getType());
    }

    @Test
    void cancelScheduledReminders_shouldCancelOnlyScheduledOnes() {
        InvoiceReminder r1 = new InvoiceReminder();
        r1.setInvoiceId(1L);
        r1.setStatus("SCHEDULED");

        InvoiceReminder r2 = new InvoiceReminder();
        r2.setInvoiceId(1L);
        r2.setStatus("SENT");

        when(reminderRepository.findByInvoiceId(1L)).thenReturn(List.of(r1, r2));

        reminderService.cancelScheduledReminders(1L);

        assertEquals("CANCELED", r1.getStatus());
        assertEquals("SENT", r2.getStatus());

        verify(reminderRepository, times(1)).save(r1);
        verify(reminderRepository, never()).save(r2);
    }

    @Test
    void dueReminders_shouldReturnScheduledRemindersUpToToday() {
        InvoiceReminder r1 = new InvoiceReminder();
        r1.setInvoiceId(1L);
        r1.setStatus("SCHEDULED");

        when(reminderRepository.findByStatusAndRemindAtLessThanEqual("SCHEDULED", LocalDate.now()))
                .thenReturn(List.of(r1));

        List<InvoiceReminder> result = reminderService.dueReminders();

        assertEquals(1, result.size());
        assertSame(r1, result.get(0));
    }
}