package com.datech.mvp.controller;

import com.datech.mvp.dto.ProjectProfitabilityDto;
import com.datech.mvp.model.Project;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.ProjectAnalyticsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository repository;
    private final CrudService crudService;
    private final ProjectAnalyticsService analyticsService;

    public ProjectController(ProjectRepository repository,
                             CrudService crudService,
                             ProjectAnalyticsService analyticsService) {
        this.repository = repository;
        this.crudService = crudService;
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public List<Project> all() {
        return crudService.findAll(repository);
    }

    @PostMapping
    public Project create(@Valid @RequestBody Project project) {
        return crudService.save(repository, project);
    }

    @GetMapping("/{id}")
    public Project one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @Valid @RequestBody Project project) {
        project.setId(id);
        return crudService.save(repository, project);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }

    @GetMapping("/{id}/profitability")
    public ProjectProfitabilityDto profitability(@PathVariable Long id) {
        return analyticsService.profitability(id);
    }

    @GetMapping("/{id}/budget-status")
    public Map<String, Boolean> budgetStatus(@PathVariable Long id) {
        return Map.of("overBudget", analyticsService.isBudgetExceeded(id));
    }
}
