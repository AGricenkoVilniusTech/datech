package com.datech.mvp.service;

import com.datech.mvp.dto.DashboardAlertsDto;
import com.datech.mvp.dto.ProjectProfitabilityDto;
import com.datech.mvp.model.Invoice;
import com.datech.mvp.model.Project;
import com.datech.mvp.model.TimeEntry;
import com.datech.mvp.repository.InvoiceRepository;
import com.datech.mvp.repository.ProjectRepository;
import com.datech.mvp.repository.TimeEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectAnalyticsService {
    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final InvoiceRepository invoiceRepository;

    public ProjectAnalyticsService(ProjectRepository projectRepository,
                                   TimeEntryRepository timeEntryRepository,
                                   InvoiceRepository invoiceRepository) {
        this.projectRepository = projectRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public ProjectProfitabilityDto profitability(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        BigDecimal totalHours = timeEntryRepository.findByProjectId(projectId).stream()
                .map(TimeEntry::getHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenue = totalHours.multiply(project.getHourlyRate())
                .setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal profitability = revenue.subtract(project.getBudget())
                .setScale(2, java.math.RoundingMode.HALF_UP);
        
        boolean overBudget = revenue.compareTo(project.getBudget()) > 0;

        return new ProjectProfitabilityDto(projectId, project.getBudget(), revenue, profitability, overBudget);
    }

    public boolean isBudgetExceeded(Long projectId) {
        return profitability(projectId).isOverBudget();
    }

    public List<Invoice> overdueInvoices() {
        return invoiceRepository.findByStatusAndDueDateBefore("UNPAID", LocalDate.now());
    }

    public DashboardAlertsDto dashboardAlerts() {
        List<Project> allProjects = projectRepository.findAll();
        List<Project> overBudgetProjects = new ArrayList<>();

        for (Project project : allProjects) {
            if (isBudgetExceeded(project.getId())) {
                overBudgetProjects.add(project);
            }
        }

        return new DashboardAlertsDto(overBudgetProjects, overdueInvoices());
    }
}
