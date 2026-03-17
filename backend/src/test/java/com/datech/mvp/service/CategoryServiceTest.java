package com.datech.mvp.service;

import com.datech.mvp.dto.CategoryCreateRequest;
import com.datech.mvp.dto.CategoryUpdateRequest;
import com.datech.mvp.model.Category;
import com.datech.mvp.repository.CategoryRepository;
import com.datech.mvp.repository.TransactionEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TransactionEntryRepository transactionEntryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_success() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Food");
        request.setType("expense");
        request.setColor("#112233");

        Category saved = new Category();
        saved.setId(10L);
        saved.setUserId(1L);
        saved.setName("Food");
        saved.setType("expense");
        saved.setColor("#112233");

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Food")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.createCategory(request);

        assertEquals(10L, result.getId());
        assertEquals("Food", result.getName());
    }

    @Test
    void createCategory_duplicateName() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("Food");
        request.setType("expense");

        when(categoryRepository.existsByUserIdAndNameIgnoreCase(1L, "Food")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.createCategory(request));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void createCategory_emptyName() {
        CategoryCreateRequest request = new CategoryCreateRequest();
        request.setName("   ");
        request.setType("expense");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.createCategory(request));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void updateCategory_success() {
        Category existing = new Category();
        existing.setId(1L);
        existing.setUserId(1L);
        existing.setName("Food");
        existing.setType("expense");

        when(categoryRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(1L, "Groceries", 1L)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.updateCategory(1L, updateName("Groceries"));

        assertEquals("Groceries", result.getName());
    }

    @Test
    void updateCategory_notOwner() {
        when(categoryRepository.findByIdAndUserId(22L, 1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.updateCategory(22L, updateName("Any")));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void deleteCategory_success() {
        Category existing = new Category();
        existing.setId(5L);
        existing.setUserId(1L);

        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
        when(transactionEntryRepository.countByUserIdAndCategoryId(1L, 5L)).thenReturn(0L);

        categoryService.deleteCategory(5L);

        verify(categoryRepository).delete(existing);
    }

    @Test
    void deleteCategory_hasLinkedTransactions() {
        Category existing = new Category();
        existing.setId(5L);
        existing.setUserId(1L);

        when(categoryRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(existing));
        when(transactionEntryRepository.countByUserIdAndCategoryId(1L, 5L)).thenReturn(2L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.deleteCategory(5L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void getCategories_returnsOnlyOwnCategories() {
        Category one = new Category();
        one.setId(1L);
        one.setUserId(1L);

        when(categoryRepository.findByUserIdOrderByTypeAscNameAsc(1L)).thenReturn(List.of(one));

        List<Category> result = categoryService.getCategories();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    private CategoryUpdateRequest updateName(String name) {
        CategoryUpdateRequest request = new CategoryUpdateRequest();
        request.setName(name);
        return request;
    }
}