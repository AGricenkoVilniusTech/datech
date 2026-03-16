package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.InvoiceReminderService;
import com.datech.mvp.service.ProjectAnalyticsService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceRepository repository;
    private final CrudService crudService;
    private final ProjectAnalyticsService analyticsService;
    private final InvoiceReminderService reminderService;

    public InvoiceController(InvoiceRepository repository,
                            CrudService crudService,
                            ProjectAnalyticsService analyticsService,
                            InvoiceReminderService reminderService) {
        this.repository = repository;
        this.crudService = crudService;
        this.analyticsService = analyticsService;
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<Invoice> all() {
        return crudService.findAll(repository);
    }

    @PostMapping
    public Invoice create(@Valid @RequestBody Invoice invoice) {
        validateInvoiceReminderSettings(invoice);

        Invoice saved = crudService.save(repository, invoice);
        reminderService.createRemindersFromInvoice(saved);
        return saved;
    }

    @GetMapping("/{id}")
    public Invoice one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }


    @PutMapping("/{id}")
    public Invoice update(@PathVariable Long id, @Valid @RequestBody Invoice invoice) {
        invoice.setId(id);
        validateInvoiceReminderSettings(invoice);

        Invoice saved = crudService.save(repository, invoice);

        if ("PAID".equalsIgnoreCase(saved.getStatus())) {
            reminderService.cancelScheduledReminders(saved.getId());
        }

        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }

    @GetMapping("/overdue")
    public List<Invoice> overdue() {
        return analyticsService.overdueInvoices();
    }

    private void validateInvoiceReminderSettings(Invoice invoice) {
        if (invoice.getDueDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date is required");
        }

        if (invoice.getDueDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Due date cannot be in the past");
        }

        boolean anyReminderSelected =
                Boolean.TRUE.equals(invoice.getRemind3DaysBefore()) ||
                Boolean.TRUE.equals(invoice.getRemind1DayBefore()) ||
                Boolean.TRUE.equals(invoice.getRemindOnDueDate());

        if (!anyReminderSelected) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one reminder option must be selected");
        }
    }
}
