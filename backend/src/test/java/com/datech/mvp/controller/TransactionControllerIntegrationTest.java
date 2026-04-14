package com.datech.mvp.controller;

import com.datech.mvp.model.Category;
import com.datech.mvp.model.TransactionEntry;
import com.datech.mvp.repository.CategoryRepository;
import com.datech.mvp.repository.TransactionEntryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionEntryRepository transactionEntryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long defaultUserId = 1L;

    @BeforeEach
    void setUp() {
        transactionEntryRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void testCreateAndFilterTransactionsIntegration() throws Exception {
        // 1. Create a Category
        Category category = new Category();
        category.setName("Software Subscriptions");
        category.setType("expense");
        category.setUserId(defaultUserId);
        category = categoryRepository.save(category);

        // 2. Create Transaction via POST API (Component 1)
        TransactionEntry newEntry = new TransactionEntry();
        newEntry.setCategoryId(category.getId());
        newEntry.setAmount(new BigDecimal("19.99"));
        newEntry.setDate(LocalDate.of(2026, 3, 15));
        newEntry.setDescription("GitHub Copilot");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEntry)))
                .andExpect(status().isOk());

        // 3. Create another Transaction outside filter range (Directly to repo - Component 2)
        TransactionEntry oldEntry = new TransactionEntry();
        oldEntry.setCategoryId(category.getId());
        oldEntry.setAmount(new BigDecimal("50.0"));
        oldEntry.setDate(LocalDate.of(2026, 2, 10));
        oldEntry.setDescription("Old hosting");
        oldEntry.setUserId(defaultUserId);
        transactionEntryRepository.save(oldEntry);

        // 4. Test Integration of GET with date filters (Component 1 -> Service -> Repo -> DB)
        mockMvc.perform(get("/api/transactions")
                .param("from", "2026-03-01")
                .param("to", "2026-03-31")
                .param("category_id", String.valueOf(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].description", is("GitHub Copilot")));
    }
}