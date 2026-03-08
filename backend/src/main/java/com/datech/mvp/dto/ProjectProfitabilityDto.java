package com.datech.mvp.dto;

import java.math.BigDecimal;

public class ProjectProfitabilityDto {
    private Long projectId;
    private BigDecimal budget;
    private BigDecimal revenue;
    private BigDecimal profitability;
    private boolean overBudget;

    public ProjectProfitabilityDto(Long projectId, BigDecimal budget, BigDecimal revenue, BigDecimal profitability, boolean overBudget) {
        this.projectId = projectId;
        this.budget = budget;
        this.revenue = revenue;
        this.profitability = profitability;
        this.overBudget = overBudget;
    }

    public Long getProjectId() {
        return projectId;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public BigDecimal getProfitability() {
        return profitability;
    }

    public boolean isOverBudget() {
        return overBudget;
    }
}
