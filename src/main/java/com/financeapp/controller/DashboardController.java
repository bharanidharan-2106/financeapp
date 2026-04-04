package com.financeapp.controller;

import com.financeapp.dto.CategorySummaryResponse;
import com.financeapp.dto.DashboardSummaryResponse;
import com.financeapp.dto.RecentTransactionResponse;
import com.financeapp.dto.TrendResponse;
import com.financeapp.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Aggregated financial analytics and insights for the dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get financial summary",
            description = "Returns total income, total expense, and net balance across all records. Access: ADMIN, ANALYST, VIEWER."
    )
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/category-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
            summary = "Get category-wise aggregation",
            description = "Returns the total amount spent or earned per category, sorted by amount descending. Access: ADMIN, ANALYST."
    )
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary() {
        return ResponseEntity.ok(dashboardService.getCategorySummary());
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
            summary = "Get monthly income and expense trends",
            description = "Returns monthly aggregated income and expense totals ordered chronologically. Period format: 'YYYY-MM'. Access: ADMIN, ANALYST."
    )
    public ResponseEntity<List<TrendResponse>> getMonthlyTrends() {
        return ResponseEntity.ok(dashboardService.getMonthlyTrends());
    }

    @GetMapping("/trends/weekly")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    @Operation(
            summary = "Get weekly income and expense trends",
            description = "Returns weekly aggregated income and expense totals ordered chronologically. Period format: 'Week-WW'. Access: ADMIN, ANALYST."
    )
    public ResponseEntity<List<TrendResponse>> getWeeklyTrends() {
        return ResponseEntity.ok(dashboardService.getWeeklyTrends());
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get recent transactions",
            description = "Returns the 5 most recent financial records ordered by date descending. Access: ADMIN, ANALYST, VIEWER."
    )
    public ResponseEntity<List<RecentTransactionResponse>> getRecentTransactions() {
        return ResponseEntity.ok(dashboardService.getRecentTransactions());
    }
}