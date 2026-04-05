package com.datech.mvp.controller;

import com.datech.mvp.model.Invoice;
import com.datech.mvp.repository.InvoiceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// UR-7: Client Portal - Invoice View
@RestController
@RequestMapping("/api/invoices")
public class InvoiceShareController {

    private final InvoiceRepository repository;
    private final Map<String, Map<String, Object>> shareTokens = new HashMap<>();

    public InvoiceShareController(InvoiceRepository repository) {
        this.repository = repository;
    }

    // UR-7: Generate shareable read-only link
    @PostMapping("/{id}/share")
    public Map<String, String> generateShareLink(
            @PathVariable Long id,
            @RequestParam(defaultValue = "7") int expiryDays) {

        Invoice invoice = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        if (invoice.getStatus() == null || invoice.getStatus().equalsIgnoreCase("DRAFT")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Draft invoices cannot be shared. Change status to Sent first.");
        }

        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = expiryDays > 0 ? LocalDateTime.now().plusDays(expiryDays) : null;

        Map<String, Object> tokenData = new HashMap<>();
        tokenData.put("invoiceId", id);
        tokenData.put("expiresAt", expiresAt);
        shareTokens.put(token, tokenData);

        Map<String, String> response = new HashMap<>();
        response.put("shareUrl", "/api/invoices/shared/" + token);
        response.put("token", token);
        response.put("expiresAt", expiresAt != null ? expiresAt.toString() : "never");
        return response;
    }

    // UR-7: View invoice via share token (no login required)
    @GetMapping("/shared/{token}")
    public Map<String, Object> viewSharedInvoice(@PathVariable String token) {

        Map<String, Object> tokenData = shareTokens.get(token);
        if (tokenData == null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Share link is invalid or has been revoked.");
        }

        LocalDateTime expiresAt = (LocalDateTime) tokenData.get("expiresAt");
        if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            shareTokens.remove(token);
            throw new ResponseStatusException(HttpStatus.GONE, "Share link has expired.");
        }

        Long invoiceId = (Long) tokenData.get("invoiceId");
        Invoice invoice = repository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        Map<String, Object> readOnlyView = new HashMap<>();
        readOnlyView.put("id", invoice.getId());
        readOnlyView.put("status", invoice.getStatus());
        readOnlyView.put("dueDate", invoice.getDueDate());
        readOnlyView.put("readOnly", true);
        return readOnlyView;
    }

    // UR-7: Revoke a share link
    @DeleteMapping("/shared/{token}")
    public Map<String, String> revokeShareLink(@PathVariable String token) {
        if (!shareTokens.containsKey(token)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Token not found.");
        }
        shareTokens.remove(token);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Share link revoked successfully.");
        return response;
    }
}