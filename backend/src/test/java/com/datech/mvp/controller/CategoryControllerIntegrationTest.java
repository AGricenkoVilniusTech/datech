package com.datech.mvp.controller;

import com.datech.mvp.dto.CategoryCreateRequest;
import com.datech.mvp.model.Category;
import com.datech.mvp.repository.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long defaultUserId = 1L;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void testCategoryLifecycleIntegration() throws Exception {
        // 1. Create a Category via API
        CategoryCreateRequest createRequest = new CategoryCreateRequest();
        createRequest.setName("Integration Testing");
        createRequest.setType("expense");
        createRequest.setColor("#00FF00");

        String response = mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Integration Testing")))
                .andReturn().getResponse().getContentAsString();

        Category createdCategory = objectMapper.readValue(response, Category.class);

        // 2. Fetch all Categories via API
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Integration Testing")));

        // 3. Delete Category via API
        mockMvc.perform(delete("/api/categories/" + createdCategory.getId()))
                .andExpect(status().isOk());

        // 4. Verify deletion via API
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
