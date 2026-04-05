package com.datech.mvp.service;

import com.datech.mvp.dto.CategoryCreateRequest;
import com.datech.mvp.dto.CategoryUpdateRequest;
import com.datech.mvp.model.Category;
import com.datech.mvp.repository.CategoryRepository;
import com.datech.mvp.repository.TransactionEntryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoryService {
    private static final Long DEFAULT_USER_ID = 1L;

    private final CategoryRepository categoryRepository;
    private final TransactionEntryRepository transactionEntryRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           TransactionEntryRepository transactionEntryRepository) {
        this.categoryRepository = categoryRepository;
        this.transactionEntryRepository = transactionEntryRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findByUserIdOrderByTypeAscNameAsc(DEFAULT_USER_ID);
    }

    public Category createCategory(CategoryCreateRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name cannot be empty");
        }

        if (categoryRepository.existsByUserIdAndNameIgnoreCase(DEFAULT_USER_ID, name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
        }

        Category category = new Category();
        category.setUserId(DEFAULT_USER_ID);
        category.setName(name);
        category.setType(request.getType());
        category.setColor(request.getColor());
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findByIdAndUserId(id, DEFAULT_USER_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"));

        if (request.getName() != null) {
            String updatedName = request.getName().trim();
            if (updatedName.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category name cannot be empty");
            }
            if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndIdNot(DEFAULT_USER_ID, updatedName, id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category name already exists");
            }
            category.setName(updatedName);
        }

        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }

        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findByIdAndUserId(id, DEFAULT_USER_ID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed"));

        if (transactionEntryRepository.countByUserIdAndCategoryId(DEFAULT_USER_ID, category.getId()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category has linked transactions");
        }

        categoryRepository.delete(category);
    }
}
