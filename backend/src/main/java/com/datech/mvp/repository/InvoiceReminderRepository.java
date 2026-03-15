package com.datech.mvp.repository;

import com.datech.mvp.model.InvoiceReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceReminderRepository extends JpaRepository<InvoiceReminder, Long> {
    List<InvoiceReminder> findByInvoiceId(Long invoiceId);
    List<InvoiceReminder> findByStatusAndRemindAtLessThanEqual(String status, LocalDate date);
}