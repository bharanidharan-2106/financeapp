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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    @Override
    public Map<String, Object> compareMonths(String month, String compareWith) {
        List<Object[]> currentMonth = financialRecordRepository.getMonthlyComparison(month);
        List<Object[]> previousMonth = financialRecordRepository.getMonthlyComparison(compareWith);

        Map<String, Object> result = new LinkedHashMap<>();
        
        if (!currentMonth.isEmpty()) {
            Object[] current = currentMonth.get(0);
            result.put("currentMonth", mapMonthData(month, current));
        } else {
            result.put("currentMonth", createEmptyMonthData(month));
        }

        if (!previousMonth.isEmpty()) {
            Object[] previous = previousMonth.get(0);
            result.put("compareWithMonth", mapMonthData(compareWith, previous));
        } else {
            result.put("compareWithMonth", createEmptyMonthData(compareWith));
        }

        Map<String, Double> currentData = extractMonthData(currentMonth);
        Map<String, Double> previousData = extractMonthData(previousMonth);
        
        result.put("comparison", new HashMap<String, Object>() {{
            put("incomeChange", currentData.get("income") - previousData.get("income"));
            put("expenseChange", currentData.get("expense") - previousData.get("expense"));
            put("balanceChange", (currentData.get("income") - currentData.get("expense")) 
                                  - (previousData.get("income") - previousData.get("expense")));
        }});

        return result;
    }

    private Map<String, Object> mapMonthData(String month, Object[] data) {
        return new HashMap<String, Object>() {{
            put("month", month);
            put("income", nullSafe((Double) data[1]));
            put("expense", nullSafe((Double) data[2]));
            put("balance", nullSafe((Double) data[1]) - nullSafe((Double) data[2]));
        }};
    }

    private Map<String, Object> createEmptyMonthData(String month) {
        return new HashMap<String, Object>() {{
            put("month", month);
            put("income", 0.0);
            put("expense", 0.0);
            put("balance", 0.0);
        }};
    }

    private Map<String, Double> extractMonthData(List<Object[]> data) {
        Map<String, Double> result = new HashMap<>();
        if (data.isEmpty()) {
            result.put("income", 0.0);
            result.put("expense", 0.0);
        } else {
            Object[] row = data.get(0);
            result.put("income", nullSafe((Double) row[1]));
            result.put("expense", nullSafe((Double) row[2]));
        }
        return result;
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