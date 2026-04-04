package com.financeapp.repository;

import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.TransactionType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long>, JpaSpecificationExecutor<FinancialRecord> {

    List<FinancialRecord> findByType(TransactionType type);

    List<FinancialRecord> findByCategory(String category);

    List<FinancialRecord> findByDateBetween(LocalDate startDate, LocalDate endDate);
    
    Page<FinancialRecord> findAll(Pageable pageable);

    // Use default Specification-based filtering to avoid NULL enum parameter issues
    default Page<FinancialRecord> filterRecords(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            TransactionType type,
            Pageable pageable) {
        
        Specification<FinancialRecord> spec = Specification
                .where(startDate != null ? getDateRangeSpec(startDate, endDate) : null)
                .and(category != null ? getCategorySpec(category) : null)
                .and(type != null ? getTypeSpec(type) : null);
        
        return findAll(spec, pageable);
    }
    
    static Specification<FinancialRecord> getDateRangeSpec(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate != null && endDate != null) {
                return cb.and(
                    cb.greaterThanOrEqualTo(root.get("date"), startDate),
                    cb.lessThanOrEqualTo(root.get("date"), endDate)
                );
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), startDate);
            } else if (endDate != null) {
                return cb.lessThanOrEqualTo(root.get("date"), endDate);
            }
            return null;
        };
    }
    
    static Specification<FinancialRecord> getCategorySpec(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }
    
    static Specification<FinancialRecord> getTypeSpec(TransactionType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }
 
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