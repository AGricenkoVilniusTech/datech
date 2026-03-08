package com.datech.mvp.controller;

import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.TimeEntryRepository;
import com.datech.mvp.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {
    private final TimeEntryRepository repository;
    private final CrudService crudService;

    public TimeEntryController(TimeEntryRepository repository, CrudService crudService) {
        this.repository = repository;
        this.crudService = crudService;
    }

    @GetMapping
    public List<TimeEntry> all(@RequestParam(required = false) Long projectId) {
        if (projectId != null) {
            return repository.findByProjectId(projectId);
        }
        return crudService.findAll(repository);
    }

    @PostMapping
    public TimeEntry create(@Valid @RequestBody TimeEntry entry) {
        return crudService.save(repository, entry);
    }

    @GetMapping("/{id}")
    public TimeEntry one(@PathVariable Long id) {
        return crudService.findById(repository, id);
    }

    @PutMapping("/{id}")
    public TimeEntry update(@PathVariable Long id, @Valid @RequestBody TimeEntry entry) {
        entry.setId(id);
        return crudService.save(repository, entry);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        crudService.delete(repository, id);
    }
}
