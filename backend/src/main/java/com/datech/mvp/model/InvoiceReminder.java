package com.datech.mvp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "invoice_reminders")
public class InvoiceReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long invoiceId;

    @NotNull
    private LocalDate remindAt;

    @NotNull
    private String type;

    @NotNull
    private String status = "SCHEDULED";

    public Long getId() {
        return id;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public LocalDate getRemindAt() {
        return remindAt;
    }

    public void setRemindAt(LocalDate remindAt) {
        this.remindAt = remindAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
