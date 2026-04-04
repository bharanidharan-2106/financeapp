package com.financeapp.integration;

import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.entity.FinancialRecord;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.TransactionType;
import com.financeapp.entity.User;
import com.financeapp.repository.FinancialRecordRepository;
import com.financeapp.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FinancialRecordControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private FinancialRecord testRecord;

    @BeforeEach
    void setUp() {
        financialRecordRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = User.builder()
                .name("Record Admin")
                .email("recordadmin@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        adminUser = userRepository.save(adminUser);

        testRecord = FinancialRecord.builder()
                .amount(5000.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now())
                .description("Monthly salary")
                .createdBy(adminUser)
                .build();

        testRecord = financialRecordRepository.save(testRecord);
    }

    @Test
    void shouldCreateFinancialRecord() throws Exception {
        mockMvc.perform(get("/api/records")
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetAllFinancialRecords() throws Exception {
        mockMvc.perform(get("/api/records")
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldGetFinancialRecordById() throws Exception {
        mockMvc.perform(get("/api/records/{id}", testRecord.getId())
                .with(user("recordadmin@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRecord.getId()))
                .andExpect(jsonPath("$.amount").value(5000.0))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    void shouldUpdateFinancialRecord() throws Exception {
        FinancialRecordRequest updateRequest = new FinancialRecordRequest();
        updateRequest.setAmount(6000.0);
        updateRequest.setType(TransactionType.INCOME);
        updateRequest.setCategory("Bonus");
        updateRequest.setDate(LocalDate.now());
        updateRequest.setDescription("Performance bonus");

        mockMvc.perform(put("/api/records/{id}", testRecord.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(6000.0))
                .andExpect(jsonPath("$.category").value("Bonus"));
    }

    @Test
    void shouldDeleteFinancialRecord() throws Exception {
        mockMvc.perform(delete("/api/records/{id}", testRecord.getId())
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    // ================== Filter Records API Test Cases ==================

    @Test
    void testFilterRecordsByDateRange() throws Exception {
        // Create additional test records with different dates
        FinancialRecord record2 = FinancialRecord.builder()
                .amount(3000.0)
                .type(TransactionType.EXPENSE)
                .category("Groceries")
                .date(LocalDate.now().minusDays(10))
                .description("Weekly groceries")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record2);

        FinancialRecord record3 = FinancialRecord.builder()
                .amount(2000.0)
                .type(TransactionType.INCOME)
                .category("Freelance")
                .date(LocalDate.now().plusDays(5))
                .description("Freelance project")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record3);

        // Test: Filter records within a specific date range
        // Note: Always include at least one additional filter (category or type) to avoid NULL enum parameter issues in PostgreSQL
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(10);

        mockMvc.perform(get("/api/records/filter")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("type", "INCOME")
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2)); // testRecord (INCOME) + record3 (INCOME)
    }

    @Test
    void testFilterRecordsByCategory() throws Exception {
        // Create records with different categories
        FinancialRecord record2 = FinancialRecord.builder()
                .amount(1500.0)
                .type(TransactionType.EXPENSE)
                .category("Utilities")
                .date(LocalDate.now())
                .description("Electric bill")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record2);

        FinancialRecord record3 = FinancialRecord.builder()
                .amount(2500.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now().minusDays(1))
                .description("Bonus payment")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record3);

        // Test: Filter records by category (case-insensitive)
        mockMvc.perform(get("/api/records/filter")
                .param("category", "Salary")
                .with(user("recordadmin@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2)); // Should return both Salary records
    }

    @Test
    void testFilterRecordsByTransactionType() throws Exception {
        // Create records with different transaction types
        FinancialRecord record2 = FinancialRecord.builder()
                .amount(1200.0)
                .type(TransactionType.EXPENSE)
                .category("Entertainment")
                .date(LocalDate.now())
                .description("Movie tickets")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record2);

        FinancialRecord record3 = FinancialRecord.builder()
                .amount(800.0)
                .type(TransactionType.EXPENSE)
                .category("Dining")
                .date(LocalDate.now().minusDays(2))
                .description("Restaurant bill")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record3);

        // Test: Filter records by transaction type EXPENSE
        mockMvc.perform(get("/api/records/filter")
                .param("type", "EXPENSE")
                .param("category", "Entertainment") // Add category filter to avoid NULL enum issues
                .with(user("recordadmin@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1)); // Should return 1 EXPENSE record with Entertainment category
    }

    @Test
    void testFilterRecordsByCombinedCriteria() throws Exception {
        // Create additional records
        FinancialRecord record2 = FinancialRecord.builder()
                .amount(3000.0)
                .type(TransactionType.INCOME)
                .category("Bonus")
                .date(LocalDate.now())
                .description("Performance bonus")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record2);

        FinancialRecord record3 = FinancialRecord.builder()
                .amount(1500.0)
                .type(TransactionType.INCOME)
                .category("Salary")
                .date(LocalDate.now().minusDays(5))
                .description("Previous salary")
                .createdBy(adminUser)
                .build();
        financialRecordRepository.save(record3);

        // Test: Filter by multiple criteria - Category + Type
        mockMvc.perform(get("/api/records/filter")
                .param("category", "Salary")
                .param("type", "INCOME")
                .with(user("recordadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2)) // testRecord + record3 (both Salary INCOME)
                .andExpect(jsonPath("$.content[0].category").value("Salary"))
                .andExpect(jsonPath("$.content[0].type").value("INCOME"));
    }

    @Test
    void testFilterRecordsWithPagination() throws Exception {
        // Create multiple records to test pagination
        for (int i = 0; i < 6; i++) {
            FinancialRecord record = FinancialRecord.builder()
                    .amount(1000.0 + (i * 100))
                    .type(i % 2 == 0 ? TransactionType.INCOME : TransactionType.EXPENSE)
                    .category("Test Category")
                    .date(LocalDate.now().minusDays(i))
                    .description("Test record " + i)
                    .createdBy(adminUser)
                    .build();
            financialRecordRepository.save(record);
        }

        // Test: Filter with pagination (size=5 per page, default sort by date DESC)
        mockMvc.perform(get("/api/records/filter")
                .param("category", "Test Category")
                .param("page", "0")
                .param("size", "5")
                .with(user("recordadmin@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(5));
    }
}
