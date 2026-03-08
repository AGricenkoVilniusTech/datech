package com.datech.mvp.repository;

import com.datech.mvp.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStatusAndDueDateBefore(String status, LocalDate date);
}
