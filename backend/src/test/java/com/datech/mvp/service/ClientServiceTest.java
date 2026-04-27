package com.datech.mvp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ClientServiceTest {

    private final ClientService service = new ClientService();

    @Test
    void acceptsValidClient() {
        assertDoesNotThrow(() -> {
            service.validateClient("Tech Corp", "contact@techcorp.com");
        });
    }

    @Test
    void rejectsEmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.validateClient("", "test@test.com");
        });
        assertEquals("Name is required", exception.getMessage());
    }

    @Test
    void rejectsNameTooShort() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.validateClient("A", "test@test.com");
        });
        assertEquals("Name must be between 2 and 100 characters", exception.getMessage());
    }

    @Test
    void rejectsInvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.validateClient("Valid Name", "invalid-email");
        });
        assertEquals("Invalid email format", exception.getMessage());
    }
}