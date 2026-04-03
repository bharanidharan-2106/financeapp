package com.financeapp.repository;

import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.TransactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    List<FinancialRecord> findByType(TransactionType type);

    List<FinancialRecord> findByCategory(String category);

    List<FinancialRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<FinancialRecord> findAll(Pageable pageable);

    @Query("""
            SELECT r FROM FinancialRecord r
            WHERE (:startDate IS NULL OR r.date >= :startDate)
              AND (:endDate   IS NULL OR r.date <= :endDate)
              AND (:category  IS NULL OR LOWER(CAST(r.category AS string)) = LOWER(:category))
              AND (:type      IS NULL OR r.type = :type)
            ORDER BY r.date DESC
            """)
    Page<FinancialRecord> filterRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") String category,
            @Param("type") TransactionType type,
            Pageable pageable
    );
} 