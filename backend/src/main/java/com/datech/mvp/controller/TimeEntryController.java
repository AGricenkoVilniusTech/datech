package com.datech.mvp.controller;

import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import com.datech.mvp.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    private final TimeEntryRepository repository;
    private final ProjectRepository projectRepository;
    private final CrudService crudService;

    public TimeEntryController(TimeEntryRepository repository,
                               ProjectRepository projectRepository,
                               CrudService crudService) {
        this.repository = repository;
        this.projectRepository = projectRepository;
        this.crudService = crudService;
    }

    @PostMapping
    public TimeEntry create(@Valid @RequestBody TimeEntry entry) {
        validateProject(entry.getProjectId());
        return crudService.save(repository, entry);
    }

    @PutMapping("/{id}")
    public TimeEntry update(@PathVariable Long id, @Valid @RequestBody TimeEntry entry) {
        entry.setId(id);
        validateProject(entry.getProjectId());
        return crudService.save(repository, entry);
    }

    private void validateProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project does not exist"));

        if ("ARCHIVED".equalsIgnoreCase(project.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Archived project cannot be used for new time entries");
        }
    }

    @GetMapping
    public List<TimeEntry> all(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return repository.findByProjectId(projectId);
        }
        return crudService.findAll(repository);
    }

    @GetMapping("/{id}")
    public TimeEntry one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }
}