package com.datech.mvp.dto;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.model.Project;

import java.util.List;

public class DashboardAlertsDto {
    private List<Project> overBudgetProjects;
    private List<Invoice> overdueInvoices;

    public DashboardAlertsDto(List<Project> overBudgetProjects, List<Invoice> overdueInvoices) {
        this.overBudgetProjects = overBudgetProjects;
        this.overdueInvoices = overdueInvoices;
    }

    public List<Project> getOverBudgetProjects() {
        return overBudgetProjects;
    }

    public List<Invoice> getOverdueInvoices() {
        return overdueInvoices;
    }
}
