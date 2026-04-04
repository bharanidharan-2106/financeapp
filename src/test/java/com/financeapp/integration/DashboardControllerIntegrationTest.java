package com.financeapp.integration;

import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.TransactionType;
import com.financeapp.entity.User;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        financialRecordRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Dashboard Test User")
                .email("dashboard@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        testUser = userRepository.save(testUser);

        FinancialRecord incomeRecord = FinancialRecord.builder()
                .amount(5000.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .description("Monthly salary")
                .createdBy(testUser)
                .build();

        FinancialRecord expenseRecord = FinancialRecord.builder()
                .amount(1000.0)
                .type(TransactionType.EXPENSE)
                .category("Food")
                .date(LocalDate.now())
                .description("Groceries")
                .createdBy(testUser)
                .build();

        financialRecordRepository.save(incomeRecord);
        financialRecordRepository.save(expenseRecord);
    }

    @Test
    void shouldGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary")
                .with(user("dashboard@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").value(5000.0))
                .andExpect(jsonPath("$.totalExpense").value(1000.0))
                .andExpect(jsonPath("$.netBalance").value(4000.0));
    }

    @Test
    void shouldGetCategorySummary() throws Exception {
        mockMvc.perform(get("/api/dashboard/category-summary")
                .with(user("dashboard@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetRecentTransactions() throws Exception {
        mockMvc.perform(get("/api/dashboard/recent")
                .with(user("dashboard@test.com").roles("VIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldFailGetCategorySummaryWithViewerRole() throws Exception {
        mockMvc.perform(get("/api/dashboard/category-summary")
                .with(user("dashboard@test.com").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetMonthlyTrends() throws Exception {
        mockMvc.perform(get("/api/dashboard/trends")
                .with(user("dashboard@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
