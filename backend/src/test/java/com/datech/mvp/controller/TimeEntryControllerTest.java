package com.datech.mvp.controller;

import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import com.datech.mvp.service.CrudService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeEntryControllerTest {

    @Mock
    private TimeEntryRepository repository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CrudService crudService;

    @InjectMocks
    private TimeEntryController controller;

    private TimeEntry entry;

    @BeforeEach
    void setUp() {
        entry = new TimeEntry();
        entry.setProjectId(1L);
        entry.setDate(LocalDate.of(2026, 3, 16));
        entry.setHours(new BigDecimal("2.50"));
        entry.setDescription("Design work");
    }

    @Test
    void create_shouldSaveWhenProjectIsActive() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus("ACTIVE");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(crudService.save(repository, entry)).thenReturn(entry);

        TimeEntry result = controller.create(entry);

        assertSame(entry, result);
        verify(crudService).save(repository, entry);
    }

    @Test
    void create_shouldRejectArchivedProject() {
        Project project = new Project();
        project.setId(1L);
        project.setStatus("ARCHIVED");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(entry));

        assertTrue(ex.getReason().contains("Archived project"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void create_shouldRejectMissingProject() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(entry));

        assertTrue(ex.getReason().contains("Project does not exist"));
        verify(crudService, never()).save(any(), any());
    }
}
