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
import com.datech.mvp.service.ProjectAnalyticsService;
import com.datech.mvp.service.TaxCalculator;

import jakarta.validation.Valid;

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
        this.repository = repository;
        this.taxCalculator = taxCalculator;
        this.crudService = crudService;
        this.analyticsService = analyticsService;
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

        BigDecimal subtotal = invoice.getAmount();

        BigDecimal taxRate = BigDecimal.valueOf(
                invoice.getTaxRate() != null ? invoice.getTaxRate() : 0
        );

        BigDecimal taxAmount = taxCalculator.calculateTax(subtotal, taxRate);
        BigDecimal total = taxCalculator.calculateTotal(subtotal, taxRate);

        invoice.setTaxAmount(taxAmount.doubleValue());
        invoice.setAmount(total);

        return repository.save(invoice);
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

    // @PostMapping("/generate")
    // public Invoice generateInvoice(@RequestBody InvoiceRequest request) {
    //     long start = System.currentTimeMillis();
    //     Invoice invoice = invoiceService.generate(request);
    //     long duration = System.currentTimeMillis() - start;
    //     System.out.println("Invoice generation took: " + duration + " ms");
    //     return invoice;
    // }
}
