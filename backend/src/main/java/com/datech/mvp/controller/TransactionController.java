package com.datech.mvp.controller;

import com.datech.mvp.model.TransactionEntry;
import com.datech.mvp.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionEntry> all(@RequestParam(required = false) String from,
                                      @RequestParam(required = false) String to,
                                      @RequestParam(name = "category_id", required = false) Long categoryId) {
        return transactionService.getTransactions(from, to, categoryId);
    }

    @PostMapping
    public TransactionEntry create(@Valid @RequestBody TransactionEntry entry) {
        return transactionService.create(entry);
    }
}