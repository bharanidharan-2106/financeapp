package com.financeapp.service;

import com.financeapp.dto.CategorySummaryResponse;
import com.financeapp.dto.DashboardSummaryResponse;
import com.financeapp.dto.RecentTransactionResponse;
import com.financeapp.dto.TrendResponse;

import java.util.List;

public interface DashboardService {

    /**
     * Returns aggregated totals: income, expense, and net balance.
     */
    DashboardSummaryResponse getSummary();

    /**
     * Returns total amount grouped by category, sorted descending by amount.
     */
    List<CategorySummaryResponse> getCategorySummary();

    /**
     * Returns monthly income and expense totals ordered chronologically.
     * Period label format: "YYYY-MM" (e.g. "2026-04")
     */
    List<TrendResponse> getMonthlyTrends();

    /**
     * Returns weekly income and expense totals ordered chronologically.
     * Period label format: "Week-WW" (e.g. "Week-14")
     */
    List<TrendResponse> getWeeklyTrends();

    /**
     * Returns the 5 most recent transactions ordered by date descending.
     */
    List<RecentTransactionResponse> getRecentTransactions();
}