package com.financeapp.service;

import com.financeapp.dto.CategorySummaryResponse;
import com.financeapp.dto.DashboardSummaryResponse;
import com.financeapp.dto.RecentTransactionResponse;
import com.financeapp.dto.TrendResponse;

import java.util.List;
import java.util.Map;

public interface DashboardService {

    DashboardSummaryResponse getSummary();

    List<CategorySummaryResponse> getCategorySummary();

    List<TrendResponse> getMonthlyTrends();

    List<TrendResponse> getWeeklyTrends();

    List<RecentTransactionResponse> getRecentTransactions();

    Map<String, Object> compareMonths(String month, String compareWith);
}