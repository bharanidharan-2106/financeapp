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
 
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'INCOME'")
    Double getTotalIncome();
 
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r WHERE r.type = 'EXPENSE'")
    Double getTotalExpense();
 
    @Query("""
            SELECT r.category, SUM(r.amount)
            FROM FinancialRecord r
            GROUP BY r.category
            ORDER BY SUM(r.amount) DESC
            """)
    List<Object[]> getCategorySummary();
 
    @Query("""
            SELECT FUNCTION('TO_CHAR', r.date, 'YYYY-MM'),
                   SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END),
                   SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END)
            FROM FinancialRecord r
            GROUP BY FUNCTION('TO_CHAR', r.date, 'YYYY-MM')
            ORDER BY 1 ASC
            """)
    List<Object[]> getMonthlyTrends();
 
    @Query("""
            SELECT CONCAT('Week-', FUNCTION('TO_CHAR', r.date, 'IW')),
                   SUM(CASE WHEN r.type = 'INCOME'  THEN r.amount ELSE 0 END),
                   SUM(CASE WHEN r.type = 'EXPENSE' THEN r.amount ELSE 0 END)
            FROM FinancialRecord r
            GROUP BY FUNCTION('TO_CHAR', r.date, 'IW')
            ORDER BY 1 ASC
            """)
    List<Object[]> getWeeklyTrends();
 
    List<FinancialRecord> findTop5ByOrderByDateDesc();
} 