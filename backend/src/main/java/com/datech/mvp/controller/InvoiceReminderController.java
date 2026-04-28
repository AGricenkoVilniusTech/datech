package com.datech.mvp.controller;

import com.datech.mvp.model.InvoiceReminder;
import com.datech.mvp.service.InvoiceReminderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoice-reminders")
public class InvoiceReminderController {

    private final InvoiceReminderService reminderService;

    public InvoiceReminderController(InvoiceReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<InvoiceReminder> all(@RequestParam(required = false) Long invoiceId) {
        if (invoiceId != null) {
            return reminderService.getByInvoiceId(invoiceId);
        }
        return reminderService.dueReminders();
    }

    @GetMapping("/all")
    public List<InvoiceReminder> allReminders() {
    return reminderService.getAllReminders();
}
}