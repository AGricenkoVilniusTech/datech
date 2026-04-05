package com.datech.mvp.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.InvoiceReminderService;
import com.datech.mvp.service.ProjectAnalyticsService;
import com.datech.mvp.service.TaxCalculator;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.datech.mvp.service.InvoicePdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository repository;
    private final CrudService crudService;
    private final ProjectAnalyticsService analyticsService;
    private final TaxCalculator taxCalculator;

    // public InvoiceController(InvoiceRepository repository, TaxCalculator taxCalculator) {
    //     this.repository = repository;
    //     this.taxCalculator = taxCalculator;
    // }
    public InvoiceController(InvoiceRepository repository, TaxCalculator taxCalculator, CrudService crudService, ProjectAnalyticsService analyticsService) {
    private final InvoicePdfService invoicePdfService;
    private final InvoiceReminderService reminderService;

    public InvoiceController(InvoiceRepository repository, CrudService crudService, ProjectAnalyticsService analyticsService, InvoicePdfService invoicePdfService, reminderService) {
        this.repository = repository;
        this.taxCalculator = taxCalculator;
        this.crudService = crudService;
        this.analyticsService = analyticsService;
        this.invoicePdfService = invoicePdfService;
        this.reminderService = reminderService;
    }

    @GetMapping
    public List<Invoice> all() {
        return crudService.findAll(repository);
    }

    // @PostMapping
    // public Invoice create(@Valid @RequestBody Invoice invoice) {
    //     return crudService.save(repository, invoice);
    // }
    @PostMapping
    public Invoice create(@Valid @RequestBody Invoice invoice) {
        validateInvoiceReminderSettings(invoice);
        BigDecimal subtotal = invoice.getAmount();
        BigDecimal taxRate = BigDecimal.valueOf(
                invoice.getTaxRate() != null ? invoice.getTaxRate() : 0
        );
        BigDecimal taxAmount = taxCalculator.calculateTax(subtotal, taxRate);
        BigDecimal total = taxCalculator.calculateTotal(subtotal, taxRate);
        invoice.setTaxAmount(taxAmount.doubleValue());
        invoice.setAmount(total);
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

    // @PostMapping("/generate")
    // public Invoice generateInvoice(@RequestBody InvoiceRequest request) {
    //     long start = System.currentTimeMillis();
    //     Invoice invoice = invoiceService.generate(request);
    //     long duration = System.currentTimeMillis() - start;
    //     System.out.println("Invoice generation took: " + duration + " ms");
    //     return invoice;
    // }
    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(
            @RequestParam Long clientId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        byte[] pdf = invoicePdfService.generateInvoicePdf(
                clientId,
                LocalDate.parse(startDate),
                LocalDate.parse(endDate)
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
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
