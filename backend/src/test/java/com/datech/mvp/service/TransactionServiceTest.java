package com.datech.mvp.service;

import com.datech.mvp.model.Category;
import com.datech.mvp.model.TransactionEntry;
import com.datech.mvp.repository.CategoryRepository;
import com.datech.mvp.repository.TransactionEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionEntryRepository transactionEntryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getTransactions_noFilters() {
        when(transactionEntryRepository.findFiltered(1L, null, null, null)).thenReturn(List.of(new TransactionEntry()));

        List<TransactionEntry> result = transactionService.getTransactions(null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_withDateRange() {
        when(transactionEntryRepository.findFiltered(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null))
                .thenReturn(List.of(new TransactionEntry()));

        List<TransactionEntry> result = transactionService.getTransactions("2026-01-01", "2026-01-31", null);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_withCategory() {
        Category category = new Category();
        category.setId(5L);
        category.setUserId(1L);

        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));
        when(transactionEntryRepository.findFiltered(1L, null, null, 5L)).thenReturn(List.of(new TransactionEntry()));

        List<TransactionEntry> result = transactionService.getTransactions(null, null, 5L);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_withAllFilters() {
        Category category = new Category();
        category.setId(5L);
        category.setUserId(1L);

        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(category));
        when(transactionEntryRepository.findFiltered(1L, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), 5L))
                .thenReturn(List.of(new TransactionEntry()));

        List<TransactionEntry> result = transactionService.getTransactions("2026-01-01", "2026-01-31", 5L);

        assertEquals(1, result.size());
    }

    @Test
    void getTransactions_fromAfterTo() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> transactionService.getTransactions("2026-02-01", "2026-01-01", null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getTransactions_invalidDateFormat() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> transactionService.getTransactions("01-01-2026", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getTransactions_categoryNotOwnedByUser() {
        when(categoryRepository.findByIdAndUserId(88L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> transactionService.getTransactions(null, null, 88L));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getTransactions_noResults() {
        when(transactionEntryRepository.findFiltered(1L, null, null, null)).thenReturn(List.of());

        List<TransactionEntry> result = transactionService.getTransactions(null, null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}