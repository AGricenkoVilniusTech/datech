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
              AND (:hasFromDate = false OR t.date >= :fromDate)
              AND (:hasToDate = false OR t.date <= :toDate)
              AND (:hasCategory = false OR t.categoryId = :categoryId)
            ORDER BY t.date DESC
            """)
    List<TransactionEntry> findFiltered(
            @Param("userId") Long userId,
            @Param("hasFromDate") boolean hasFromDate,
            @Param("fromDate") LocalDate fromDate,
            @Param("hasToDate") boolean hasToDate,
            @Param("toDate") LocalDate toDate,
            @Param("hasCategory") boolean hasCategory,
            @Param("categoryId") Long categoryId
    );
}
