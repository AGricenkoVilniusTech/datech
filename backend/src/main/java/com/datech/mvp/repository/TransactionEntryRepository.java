package com.datech.mvp.repository;

import com.datech.mvp.model.TransactionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {
    long countByUserIdAndCategoryId(Long userId, Long categoryId);

    @Query("""
            SELECT t FROM TransactionEntry t
            WHERE t.userId = :userId
              AND (:fromDate IS NULL OR t.date >= :fromDate)
              AND (:toDate IS NULL OR t.date <= :toDate)
              AND (:categoryId IS NULL OR t.categoryId = :categoryId)
            ORDER BY t.date DESC
            """)
    List<TransactionEntry> findFiltered(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("categoryId") Long categoryId
    );
}
