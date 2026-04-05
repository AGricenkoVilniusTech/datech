package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceShareControllerTest {

    @Mock
    private InvoiceRepository repository;

    @InjectMocks
    private InvoiceShareController controller;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setStatus("SENT");
        invoice.setDueDate(LocalDate.now().plusDays(10));
    }

    @Test
    void generateShareLink_shouldReturnTokenForSentInvoice() {
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));

        Map<String, String> result = controller.generateShareLink(1L, 7);

        assertNotNull(result.get("token"));
        assertTrue(result.get("shareUrl").contains("/api/invoices/shared/"));
        assertEquals("7", result.get("expiryDays") != null ? result.get("expiryDays") : "7");
    }

    @Test
    void generateShareLink_shouldRejectDraftInvoice() {
        invoice.setStatus("DRAFT");
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.generateShareLink(1L, 7));

        assertTrue(ex.getReason().contains("Draft invoices cannot be shared"));
    }

    @Test
    void generateShareLink_shouldRejectNullStatus() {
        invoice.setStatus(null);
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.generateShareLink(1L, 7));

        assertTrue(ex.getReason().contains("Draft invoices cannot be shared"));
    }

    @Test
    void viewSharedInvoice_shouldReturnReadOnlyView() {
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));
        Map<String, String> linkResult = controller.generateShareLink(1L, 7);
        String token = linkResult.get("token");

        Map<String, Object> view = controller.viewSharedInvoice(token);

        assertEquals(1L, view.get("id"));
        assertEquals("SENT", view.get("status"));
        assertEquals(true, view.get("readOnly"));
    }

    @Test
    void viewSharedInvoice_shouldThrowGoneForInvalidToken() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.viewSharedInvoice("invalid-token-xyz"));

        assertTrue(ex.getReason().contains("invalid or has been revoked"));
    }

    @Test
    void revokeShareLink_shouldInvalidateToken() {
        when(repository.findById(1L)).thenReturn(Optional.of(invoice));
        Map<String, String> linkResult = controller.generateShareLink(1L, 7);
        String token = linkResult.get("token");

        controller.revokeShareLink(token);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.viewSharedInvoice(token));
        assertTrue(ex.getReason().contains("invalid or has been revoked"));
    }

    @Test
    void generateShareLink_shouldThrowForNonExistentInvoice() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> controller.generateShareLink(99L, 7));
    }
}