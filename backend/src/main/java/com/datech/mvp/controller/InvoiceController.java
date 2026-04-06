package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.ProjectAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.datech.mvp.service.InvoicePdfService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceRepository repository;
    private final CrudService crudService;
    private final ProjectAnalyticsService analyticsService;
    private final InvoicePdfService invoicePdfService;

<<<<<<< Updated upstream
    public InvoiceController(InvoiceRepository repository, CrudService crudService, ProjectAnalyticsService analyticsService, InvoicePdfService invoicePdfService) {
=======
    public InvoiceController(InvoiceRepository repository, CrudService crudService, ProjectAnalyticsService analyticsService, InvoicePdfService invoicePdfService, InvoiceReminderService reminderService) {
>>>>>>> Stashed changes
        this.repository = repository;
        this.crudService = crudService;
        this.analyticsService = analyticsService;
        this.invoicePdfService = invoicePdfService;
    }

    @GetMapping
    public List<Invoice> all() {
        return crudService.findAll(repository);
    }

    @PostMapping
    public Invoice create(@Valid @RequestBody Invoice invoice) {
        return crudService.save(repository, invoice);
    }

    @GetMapping("/{id}")
    public Invoice one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public Invoice update(@PathVariable Long id, @Valid @RequestBody Invoice invoice) {
        invoice.setId(id);
        return crudService.save(repository, invoice);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }

    @GetMapping("/overdue")
    public List<Invoice> overdue() {
        return analyticsService.overdueInvoices();
    }

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
}
