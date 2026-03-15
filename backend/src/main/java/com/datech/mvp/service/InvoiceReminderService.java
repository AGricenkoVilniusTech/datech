package com.datech.mvp.service;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.model.InvoiceReminder;
import com.datech.mvp.repository.InvoiceReminderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceReminderService {

    private final InvoiceReminderRepository reminderRepository;

    public InvoiceReminderService(InvoiceReminderRepository reminderRepository) {
        this.reminderRepository = reminderRepository;
    }

    public List<InvoiceReminder> getByInvoiceId(Long invoiceId) {
        return reminderRepository.findByInvoiceId(invoiceId);
    }

    public void createDefaultReminders(Invoice invoice) {
        LocalDate dueDate = invoice.getDueDate();

        createReminder(invoice.getId(), dueDate.minusDays(3), "DUE_MINUS_3");
        createReminder(invoice.getId(), dueDate.minusDays(1), "DUE_MINUS_1");
        createReminder(invoice.getId(), dueDate, "DUE_TODAY");
    }

    private void createReminder(Long invoiceId, LocalDate remindAt, String type) {
        InvoiceReminder reminder = new InvoiceReminder();
        reminder.setInvoiceId(invoiceId);
        reminder.setRemindAt(remindAt);
        reminder.setType(type);
        reminder.setStatus("SCHEDULED");
        reminderRepository.save(reminder);
    }

    public void cancelScheduledReminders(Long invoiceId) {
        List<InvoiceReminder> reminders = reminderRepository.findByInvoiceId(invoiceId);
        for (InvoiceReminder reminder : reminders) {
            if ("SCHEDULED".equals(reminder.getStatus())) {
                reminder.setStatus("CANCELED");
                reminderRepository.save(reminder);
            }
        }
    }

    public List<InvoiceReminder> dueReminders() {
        return reminderRepository.findByStatusAndRemindAtLessThanEqual("SCHEDULED", LocalDate.now());
    }
}
