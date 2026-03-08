package com.datech.mvp.controller;

import com.datech.mvp.dto.DashboardAlertsDto;
import com.datech.mvp.service.ProjectAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final ProjectAnalyticsService analyticsService;

    public DashboardController(ProjectAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/alerts")
    public DashboardAlertsDto alerts() {
        return analyticsService.dashboardAlerts();
    }
}
