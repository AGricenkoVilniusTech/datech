package com.datech.mvp.controller;

import com.datech.mvp.model.Project;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.service.CrudService;
import com.datech.mvp.service.ProjectAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private CrudService crudService;

    @Mock
    private ProjectAnalyticsService analyticsService;

    @InjectMocks
    private ProjectController controller;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Website Redesign");
        project.setClientId(10L);
        project.setBudget(new BigDecimal("1000.00"));
        project.setHourlyRate(new BigDecimal("50.00"));
        project.setCurrency("eur");
        project.setStatus("active");
    }

    @Test
    void create_shouldNormalizeCurrencyAndStatusAndSave() {
        when(repository.findByClientIdAndNameIgnoreCase(10L, "Website Redesign"))
                .thenReturn(Optional.empty());
        when(crudService.save(repository, project)).thenReturn(project);

        Project result = controller.create(project);

        assertEquals("EUR", project.getCurrency());
        assertEquals("ACTIVE", project.getStatus());
        assertSame(project, result);
        verify(crudService).save(repository, project);
    }

    @Test
    void create_shouldRejectInvalidCurrencyLength() {
        project.setCurrency("EU");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(project));

        assertTrue(ex.getReason().contains("3-letter code"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void create_shouldRejectDuplicateProjectName() {
        Project existing = new Project();
        existing.setId(99L);

        when(repository.findByClientIdAndNameIgnoreCase(10L, "Website Redesign"))
                .thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(project));

        assertTrue(ex.getReason().contains("unique per client"));
        verify(crudService, never()).save(any(), any());
    }

    @Test
    void update_shouldAllowSameRecordDuplicateCheck() {
        Project existing = new Project();
        existing.setId(1L);

        when(repository.findByClientIdAndNameIgnoreCase(10L, "Website Redesign"))
                .thenReturn(Optional.of(existing));
        when(crudService.save(repository, project)).thenReturn(project);

        Project result = controller.update(1L, project);

        assertEquals(1L, result.getId());
        verify(crudService).save(repository, project);
    }

    @Test
    void create_shouldRejectInvalidStatus() {
        project.setStatus("DELETED");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> controller.create(project));

        assertTrue(ex.getReason().contains("ACTIVE or ARCHIVED"));
        verify(crudService, never()).save(any(), any());
    }
}