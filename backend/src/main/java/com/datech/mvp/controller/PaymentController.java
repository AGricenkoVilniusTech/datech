package com.datech.mvp.controller;

import com.datech.mvp.model.Payment;
import com.datech.mvp.repository.PaymentRepository;
import com.datech.mvp.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository repository;
    private final CrudService crudService;

    public PaymentController(PaymentRepository repository, CrudService crudService) {
        this.repository = repository;
        this.crudService = crudService;
    }

    @GetMapping
    public List<Payment> all(@RequestParam(required = false) Long invoiceId) {
        if (invoiceId != null) {
            return repository.findByInvoiceId(invoiceId);
        }
        return crudService.findAll(repository);
    }

    @PostMapping
    public Payment create(@Valid @RequestBody Payment payment) {
        return crudService.save(repository, payment);
    }

    @GetMapping("/{id}")
    public Payment one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public Payment update(@PathVariable Long id, @Valid @RequestBody Payment payment) {
        payment.setId(id);
        return crudService.save(repository, payment);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }
}
