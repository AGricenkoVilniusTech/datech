package com.datech.mvp.controller;

import com.datech.mvp.dto.ProjectProfitabilityDto;
import com.datech.mvp.model.Project;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.ProjectAnalyticsService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        normalizeAndValidate(project, null);
        return crudService.save(repository, project);
    }

    @GetMapping("/{id}")
    public Project one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @Valid @RequestBody Project project) {
        project.setId(id);
        normalizeAndValidate(project, id);
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

    private void normalizeAndValidate(Project project, Long currentId) {
        if (project.getCurrency() == null || project.getCurrency().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency is required");
        }

        project.setCurrency(project.getCurrency().toUpperCase());
        if (project.getCurrency().length() != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency must be a 3-letter code");
        }

        if (project.getStatus() == null || project.getStatus().isBlank()) {
            project.setStatus("ACTIVE");
        } else {
            project.setStatus(project.getStatus().toUpperCase());
        }

        if (!project.getStatus().equals("ACTIVE") && !project.getStatus().equals("ARCHIVED")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or ARCHIVED");
        }

        repository.findByClientIdAndNameIgnoreCase(project.getClientId(), project.getName())
                .ifPresent(existing -> {
                    boolean sameRecord = currentId != null && existing.getId().equals(currentId);
                    if (!sameRecord) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Project name must be unique per client"
                        );
                    }
                });
    }
}
