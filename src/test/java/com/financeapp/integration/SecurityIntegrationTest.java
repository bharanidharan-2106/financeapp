package com.financeapp.integration;

import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import com.financeapp.security.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Security Test User")
                .email("sectest@test.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.VIEWER)
                .status(Status.ACTIVE)
                .build();

        userRepository.save(testUser);
    }

    @Test
    void shouldRejectRequestWithMissingAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectGetAllRecordsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/records"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectDashboardAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCreateRecordWithoutToken() throws Exception {
        mockMvc.perform(post("/api/records")
                .contentType("application/json")
                .content("{\"amount\":5000.0,\"type\":\"INCOME\",\"category\":\"Salary\",\"date\":\"2026-04-04\",\"description\":\"test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectInvalidJwtToken() throws Exception {
        String invalidToken = "invalid.jwt.token";

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectMalformedJwtToken() throws Exception {
        String malformedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid.signature";

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + malformedToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectJwtWithoutBearerPrefix() throws Exception {
        String validToken = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                testUser.getEmail(), testUser.getPassword(), new java.util.ArrayList<>()));

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, validToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectEmptyBearerToken() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer "))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectJwtSignedWithWrongKey() throws Exception {
        byte[] wrongKey = Base64.getEncoder().encode("wrong-secret-key-that-is-different".getBytes());
        Key key = Keys.hmacShaKeyFor(wrongKey);

        String wrongSignatureToken = Jwts.builder()
                .setSubject("test@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + wrongSignatureToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectExpiredJwtToken() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        Key signingKey = Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .setSubject("sectest@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) 
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTokenExpiredWhileProcessing() throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        Key signingKey = Keys.hmacShaKeyFor(keyBytes);

        String almostExpiredToken = Jwts.builder()
                .setSubject("sectest@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1)) 
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        Thread.sleep(100);

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + almostExpiredToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectViewerRoleAccessToCreateUser() throws Exception {
        mockMvc.perform(post("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                .contentType("application/json")
                .content("{\"name\":\"New User\",\"email\":\"newuser@test.com\",\"password\":\"pass123\",\"role\":\"VIEWER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectViewerRoleAccessToCreateRecord() throws Exception {
        mockMvc.perform(post("/api/records")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                .contentType("application/json")
                .content("{\"amount\":5000.0,\"type\":\"INCOME\",\"category\":\"Salary\",\"date\":\"2026-04-04\",\"description\":\"test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectViewerRoleAccessToUpdateRecord() throws Exception {
        mockMvc.perform(put("/api/records/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                .contentType("application/json")
                .content("{\"amount\":6000.0,\"type\":\"INCOME\",\"category\":\"Bonus\",\"date\":\"2026-04-04\",\"description\":\"test\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectViewerRoleAccessToDeleteRecord() throws Exception {
        mockMvc.perform(delete("/api/records/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTokenWithInvalidFormat() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "BearerToken123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectTokenWithDifferentAuthScheme() throws Exception {
        String validToken = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                testUser.getEmail(), testUser.getPassword(), new java.util.ArrayList<>()));

        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + validToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectEmptyAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/users")
                .header(HttpHeaders.AUTHORIZATION, ""))
                .andExpect(status().isForbidden());
    }
}
