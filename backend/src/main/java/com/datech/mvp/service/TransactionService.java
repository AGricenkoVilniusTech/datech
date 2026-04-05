package com.datech.mvp.service;

import com.datech.mvp.model.TransactionEntry;
import com.datech.mvp.repository.CategoryRepository;
import com.datech.mvp.repository.TransactionEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class TransactionService {
    private static final Long DEFAULT_USER_ID = 1L;

    private final TransactionEntryRepository transactionEntryRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionEntryRepository transactionEntryRepository,
                              CategoryRepository categoryRepository) {
        this.transactionEntryRepository = transactionEntryRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TransactionEntry> getTransactions(String from, String to, Long categoryId) {
        LocalDate fromDate = parseDate(from);
        LocalDate toDate = parseDate(to);

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be before end date");
        }

        if (categoryId != null && categoryRepository.findByIdAndUserId(categoryId, DEFAULT_USER_ID).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Category does not belong to user");
        }

        return transactionEntryRepository.findFiltered(DEFAULT_USER_ID, fromDate, toDate, categoryId);
    }

    public TransactionEntry create(TransactionEntry entry) {
        entry.setUserId(DEFAULT_USER_ID);
        return transactionEntryRepository.save(entry);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }
    }
}
