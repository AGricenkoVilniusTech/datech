package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

// UR-8: Recurring Invoice Generation
@RestController
@RequestMapping("/api/invoices/recurring")
public class RecurringInvoiceController {

    private final InvoiceRepository repository;

    // In-memory schedule store: scheduleId -> schedule data
    private final Map<String, Map<String, Object>> schedules = new HashMap<>();

    public RecurringInvoiceController(InvoiceRepository repository) {
        this.repository = repository;
    }

    // UR-8: Create a recurring invoice schedule
    @PostMapping
    public Map<String, Object> createSchedule(@RequestBody Map<String, Object> request) {

        String interval = (String) request.get("interval");
        String startDateStr = (String) request.get("startDate");
        String endDateStr = (String) request.get("endDate");
        Object clientId = request.get("clientId");

        // Validation
        if (clientId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client is required.");
        }
        if (interval == null || !List.of("WEEKLY", "MONTHLY", "QUARTERLY").contains(interval.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Interval must be WEEKLY, MONTHLY, or QUARTERLY.");
        }
        if (startDateStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date is required.");
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Start date must be today or a future date.");
        }

        if (endDateStr != null) {
            LocalDate endDate = LocalDate.parse(endDateStr);
            if (!endDate.isAfter(startDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "End date must be after start date.");
            }
        }

        // Save schedule
        String scheduleId = UUID.randomUUID().toString();
        Map<String, Object> schedule = new HashMap<>();
        schedule.put("scheduleId", scheduleId);
        schedule.put("clientId", clientId);
        schedule.put("interval", interval.toUpperCase());
        schedule.put("startDate", startDateStr);
        schedule.put("endDate", endDateStr);
        schedule.put("status", "ACTIVE");
        schedules.put(scheduleId, schedule);

        return schedule;
    }

    // UR-8: Cancel a recurring schedule
    @DeleteMapping("/{scheduleId}")
    public Map<String, String> cancelSchedule(@PathVariable String scheduleId) {
        if (!schedules.containsKey(scheduleId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found.");
        }
        schedules.get(scheduleId).put("status", "CANCELLED");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Schedule cancelled successfully.");
        return response;
    }

    // UR-8: Get all schedules
    @GetMapping
    public Collection<Map<String, Object>> getAllSchedules() {
        return schedules.values();
    }
}