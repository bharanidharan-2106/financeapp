package com.financeapp.service;

import com.financeapp.dto.UserRequest;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.User;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUser() {

        UserRequest request = new UserRequest();
        request.setName("Test");
        request.setEmail("test@gmail.com");
        request.setPassword("password123");
        request.setRole(Role.ADMIN);

        User user = User.builder()
                .id(1L)
                .name("Test")
                .email("test@gmail.com")
                .password("encoded")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .build();

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        var response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Test", response.getName());
        assertEquals("test@gmail.com", response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }
}