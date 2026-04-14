package com.datech.mvp.controller;

import com.datech.mvp.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecurringInvoiceControllerTest {

    @Mock
    private InvoiceRepository repository;

    @InjectMocks
    private RecurringInvoiceController controller;

    private Map<String, Object> validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new HashMap<>();
        validRequest.put("clientId", 1L);
        validRequest.put("interval", "MONTHLY");
        validRequest.put("startDate", LocalDate.now().plusDays(1).toString());
    }

    @Test
    void createSchedule_shouldReturnScheduleForValidRequest() {
        Map<String, Object> result = controller.createSchedule(validRequest);

        assertNotNull(result.get("scheduleId"));
        assertEquals("MONTHLY", result.get("interval"));
        assertEquals("ACTIVE", result.get("status"));
    }

    @Test
    void createSchedule_shouldRejectMissingClient() {
        validRequest.remove("clientId");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createSchedule(validRequest));

        assertTrue(ex.getReason().contains("Client is required"));
    }

    @Test
    void createSchedule_shouldRejectInvalidInterval() {
        validRequest.put("interval", "DAILY");

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createSchedule(validRequest));

        assertTrue(ex.getReason().contains("WEEKLY, MONTHLY, or QUARTERLY"));
    }

    @Test
    void createSchedule_shouldRejectPastStartDate() {
        validRequest.put("startDate", LocalDate.now().minusDays(1).toString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createSchedule(validRequest));

        assertTrue(ex.getReason().contains("today or a future date"));
    }

    @Test
    void createSchedule_shouldRejectEndDateBeforeStartDate() {
        validRequest.put("endDate", LocalDate.now().toString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.createSchedule(validRequest));

        assertTrue(ex.getReason().contains("End date must be after start date"));
    }

    @Test
    void cancelSchedule_shouldSetStatusToCancelled() {
        Map<String, Object> created = controller.createSchedule(validRequest);
        String scheduleId = (String) created.get("scheduleId");

        controller.cancelSchedule(scheduleId);

        Map<String, Object> schedule = controller.getAllSchedules()
                .stream().filter(s -> s.get("scheduleId").equals(scheduleId))
                .findFirst().orElseThrow();

        assertEquals("CANCELLED", schedule.get("status"));
    }
}