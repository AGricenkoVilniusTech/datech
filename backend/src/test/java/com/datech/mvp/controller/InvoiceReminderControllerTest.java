package com.datech.mvp.controller;

import com.datech.mvp.model.InvoiceReminder;
import com.datech.mvp.service.InvoiceReminderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceReminderControllerTest {

    @Mock
    private InvoiceReminderService reminderService;

    @InjectMocks
    private InvoiceReminderController controller;

    @Test
    void all_shouldReturnDueRemindersWhenNoInvoiceId() {
        InvoiceReminder r = new InvoiceReminder();
        when(reminderService.dueReminders()).thenReturn(List.of(r));

        List<InvoiceReminder> result = controller.all(null);

        assertEquals(1, result.size());
        verify(reminderService).dueReminders();
        verify(reminderService, never()).getByInvoiceId(any());
    }

    @Test
    void all_shouldReturnByInvoiceIdWhenProvided() {
        InvoiceReminder r = new InvoiceReminder();
        when(reminderService.getByInvoiceId(5L)).thenReturn(List.of(r));

        List<InvoiceReminder> result = controller.all(5L);

        assertEquals(1, result.size());
        verify(reminderService).getByInvoiceId(5L);
        verify(reminderService, never()).dueReminders();
    }

    @Test
    void all_shouldReturnEmptyListWhenNoReminders() {
        when(reminderService.dueReminders()).thenReturn(List.of());

        List<InvoiceReminder> result = controller.all(null);

        assertTrue(result.isEmpty());
    }
}