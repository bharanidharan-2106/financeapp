package com.financeapp.integration;

import com.financeapp.dto.FinancialRecordRequest;
import com.financeapp.dto.LoginRequest;
import com.financeapp.dto.UserRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndToEndIntegrationTest {

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

    @BeforeEach
    void setUp() {
        financialRecordRepository.deleteAll();
        userRepository.deleteAll();

        adminUser = User.builder()
                .name("E2E Admin")
                .email("e2eadmin@test.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(adminUser);
    }

    @Test
    void shouldPerformCompleteWorkflow() throws Exception {
        // Step 1: Admin login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("e2eadmin@test.com");
        loginRequest.setPassword("admin123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        assert (responseBody.contains("token"));

        UserRequest userRequest = new UserRequest();
        userRequest.setName("E2E Test User");
        userRequest.setEmail("e2euser@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(Role.ANALYST);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
                .with(user("e2eadmin@test.com").roles("ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("E2E Test User"))
                .andExpect(jsonPath("$.role").value("ANALYST"));

        mockMvc.perform(get("/api/users")
                .with(user("e2eadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/dashboard/summary")
                .with(user("e2eadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netBalance").exists());

        mockMvc.perform(get("/api/dashboard/recent")
                .with(user("e2eadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/api/records?page=0&size=10")
                .with(user("e2eadmin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/records")
                .with(user("e2euser@test.com").roles("ANALYST")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard/category-summary")
                .with(user("e2euser@test.com").roles("ANALYST")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldHandleUnauthorizedAccessProperly() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("Unauthorized User");
        userRequest.setEmail("unauthorized@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(Role.VIEWER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/records"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldHandleRoleBasedAccessControl() throws Exception {
        User analystUser = User.builder()
                .name("Test Analyst")
                .email("analyst@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ANALYST)
                .status(Status.ACTIVE)
                .build();
        userRepository.save(analystUser);

        UserRequest userRequest = new UserRequest();
        userRequest.setName("New User");
        userRequest.setEmail("newuser@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(Role.VIEWER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest))
                .with(user("analyst@test.com").roles("ANALYST")))
                .andExpect(status().isForbidden());

        FinancialRecordRequest recordRequest = new FinancialRecordRequest();
        recordRequest.setAmount(5000.0);
        recordRequest.setType(TransactionType.INCOME);
        recordRequest.setCategory("Salary");
        recordRequest.setDate(LocalDate.now());
        recordRequest.setDescription("Salary");

        mockMvc.perform(post("/api/records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(recordRequest))
                .with(user("analyst@test.com").roles("ANALYST")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/records")
                .with(user("analyst@test.com").roles("ANALYST")))
                .andExpect(status().isOk());
    }
}
