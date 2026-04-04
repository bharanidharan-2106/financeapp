package com.financeapp.integration;

import com.financeapp.dto.UserRequest;
import com.financeapp.entity.Role;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRejectUserCreationWithoutAuthentication() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setName("New User");
        userRequest.setEmail("newuser@test.com");
        userRequest.setPassword("password123");
        userRequest.setRole(Role.VIEWER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidUserRequestData() throws Exception {
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}")
                .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void shouldValidateUserEndpointExistsWithAdminRole() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUserGetWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUserGetWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/users")
                .with(user("viewer@test.com").roles("VIEWER")))
                .andExpect(status().isForbidden());
    }
}
