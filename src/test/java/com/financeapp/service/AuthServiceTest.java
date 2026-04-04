package com.financeapp.service;

import com.financeapp.dto.AuthResponse;
import com.financeapp.dto.LoginRequest;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.User;
import com.financeapp.exception.InvalidCredentialsException;
import com.financeapp.security.CustomUserDetails;
import com.financeapp.security.JwtUtil;
import com.financeapp.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private LoginRequest loginRequest;
    private User testUser;
    private CustomUserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@gmail.com")
                .password("encoded")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        userDetails = new CustomUserDetails(testUser);
        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void shouldLoginSuccessfully() {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn("token123");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals("ADMIN", response.getRole());
        assertEquals("Test User", response.getUsername());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateToken(userDetails);
    }

    @Test
    void shouldThrowExceptionWhenUserIsInactive() {
        User inactiveUser = User.builder()
                .id(1L)
                .name("Inactive User")
                .email("inactive@gmail.com")
                .password("encoded")
                .role(Role.VIEWER)
                .status(Status.INACTIVE)
                .build();

        CustomUserDetails inactiveUserDetails = new CustomUserDetails(inactiveUser);
        Authentication inactiveAuthentication = new UsernamePasswordAuthenticationToken(
                inactiveUserDetails, null, inactiveUserDetails.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(inactiveAuthentication);

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenBadCredentials() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void shouldThrowExceptionWhenAccountDisabled() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Account is disabled"));

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(loginRequest);
        });

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    void shouldLoginWithViewerRole() {
        User viewerUser = User.builder()
                .id(2L)
                .name("Viewer User")
                .email("viewer@gmail.com")
                .password("encoded")
                .role(Role.VIEWER)
                .status(Status.ACTIVE)
                .build();

        CustomUserDetails viewerDetails = new CustomUserDetails(viewerUser);
        Authentication viewerAuth = new UsernamePasswordAuthenticationToken(
                viewerDetails, null, viewerDetails.getAuthorities());

        LoginRequest viewerLoginRequest = new LoginRequest();
        viewerLoginRequest.setEmail("viewer@gmail.com");
        viewerLoginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(viewerAuth);
        when(jwtUtil.generateToken(viewerDetails)).thenReturn("viewer_token123");

        AuthResponse response = authService.login(viewerLoginRequest);

        assertNotNull(response);
        assertEquals("viewer_token123", response.getToken());
        assertEquals("VIEWER", response.getRole());
        assertEquals("Viewer User", response.getUsername());
    }

    @Test
    void shouldLoginWithAnalystRole() {
        User analystUser = User.builder()
                .id(3L)
                .name("Analyst")
                .email("analyst@gmail.com")
                .password("encoded")
                .role(Role.ANALYST)
                .status(Status.ACTIVE)
                .build();

        CustomUserDetails analystDetails = new CustomUserDetails(analystUser);
        Authentication analystAuth = new UsernamePasswordAuthenticationToken(
                analystDetails, null, analystDetails.getAuthorities());

        LoginRequest analystLoginRequest = new LoginRequest();
        analystLoginRequest.setEmail("analyst@gmail.com");
        analystLoginRequest.setPassword("password123");

        when(authenticationManager.authenticate(any())).thenReturn(analystAuth);
        when(jwtUtil.generateToken(analystDetails)).thenReturn("analyst_token123");

        AuthResponse response = authService.login(analystLoginRequest);

        assertNotNull(response);
        assertEquals("analyst_token123", response.getToken());
        assertEquals("ANALYST", response.getRole());
        assertEquals("Analyst", response.getUsername());
    }
}
