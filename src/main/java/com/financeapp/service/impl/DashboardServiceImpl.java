package com.financeapp.service.impl;

import com.financeapp.dto.CategorySummaryResponse;
import com.financeapp.dto.DashboardSummaryResponse;
import com.financeapp.dto.RecentTransactionResponse;
import com.financeapp.dto.TrendResponse;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final FinancialRecordRepository financialRecordRepository;

    public DashboardServiceImpl(FinancialRecordRepository financialRecordRepository) {
        this.financialRecordRepository = financialRecordRepository;
    }

    @Override
    public DashboardSummaryResponse getSummary() {
        Double totalIncome  = nullSafe(financialRecordRepository.getTotalIncome());
        Double totalExpense = nullSafe(financialRecordRepository.getTotalExpense());
        Double netBalance   = totalIncome - totalExpense;

        return DashboardSummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .build();
    }

    @Override
    public List<CategorySummaryResponse> getCategorySummary() {
        return financialRecordRepository.getCategorySummary()
                .stream()
                .map(this::toCategorySummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendResponse> getMonthlyTrends() {
        return financialRecordRepository.getMonthlyTrends()
                .stream()
                .map(this::toTrendResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrendResponse> getWeeklyTrends() {
        return financialRecordRepository.getWeeklyTrends()
                .stream()
                .map(this::toTrendResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentTransactionResponse> getRecentTransactions() {
        return financialRecordRepository.findTop5ByOrderByDateDesc()
                .stream()
                .map(this::toRecentTransactionResponse)
                .collect(Collectors.toList());
    }

    private CategorySummaryResponse toCategorySummary(Object[] row) {
        return CategorySummaryResponse.builder()
                .category((String) row[0])
                .totalAmount(nullSafe((Double) row[1]))
                .build();
    }

    private TrendResponse toTrendResponse(Object[] row) {
        return TrendResponse.builder()
                .period((String) row[0])
                .totalIncome(nullSafe((Double) row[1]))
                .totalExpense(nullSafe((Double) row[2]))
                .build();
    }

    private RecentTransactionResponse toRecentTransactionResponse(FinancialRecord record) {
        return RecentTransactionResponse.builder()
                .id(record.getId())
                .amount(record.getAmount())
                .type(record.getType())
                .category(record.getCategory())
                .date(record.getDate())
                .description(record.getDescription())
                .build();
    }

    private Double nullSafe(Double value) {
        return value != null ? value : 0.0;
    }
}