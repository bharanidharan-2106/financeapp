package com.financeapp.controller;

import com.financeapp.dto.AdminRegisterRequest;
import com.financeapp.dto.AuthResponse;
import com.financeapp.dto.LoginRequest;
import com.financeapp.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @Operation(
            summary = "User Login",
            description = "Authenticate user using email and password and return JWT token"
        )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Admin Registration",
            description = "Register a new admin user. This endpoint creates an ADMIN role user only. " +
                          "Cannot be used to create VIEWER or ANALYST roles - those must be created by existing admins via the user management API."
        )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest adminRegisterRequest) {
        AuthResponse response = authService.registerAdmin(adminRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

