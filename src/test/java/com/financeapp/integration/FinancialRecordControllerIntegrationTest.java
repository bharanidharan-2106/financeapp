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
}
