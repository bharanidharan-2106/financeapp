package com.financeapp.service.impl;

import com.financeapp.dto.AdminRegisterRequest;
import com.financeapp.dto.AuthResponse;
import com.financeapp.dto.LoginRequest;
import com.financeapp.entity.Role;
import com.financeapp.entity.Status;
import com.financeapp.entity.User;
import com.financeapp.exception.DuplicateEmailException;
import com.financeapp.exception.InvalidCredentialsException;
import com.financeapp.repository.UserRepository;
import com.financeapp.security.CustomUserDetails;
import com.financeapp.security.JwtUtil;
import com.financeapp.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                         UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            if (userDetails.getUser().getStatus() == Status.INACTIVE) {
                throw new InvalidCredentialsException("Account is inactive. Please contact an administrator.");
            }

            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .role(userDetails.getUser().getRole().name())
                    .username(userDetails.getUser().getName())
                    .build();

        } catch (DisabledException e) {
            throw new InvalidCredentialsException("Account is inactive. Please contact an administrator.");
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }
    }

    @Override
    public AuthResponse registerAdmin(AdminRegisterRequest adminRegisterRequest) {
        if (userRepository.existsByEmail(adminRegisterRequest.getEmail())) {
            throw new DuplicateEmailException(adminRegisterRequest.getEmail());
        }

        User newAdmin = User.builder()
                .name(adminRegisterRequest.getName())
                .email(adminRegisterRequest.getEmail())
                .password(passwordEncoder.encode(adminRegisterRequest.getPassword()))
                .role(Role.ADMIN)  
                .status(Status.ACTIVE)
                .build();

        User savedAdmin = userRepository.save(newAdmin);

        CustomUserDetails userDetails = new CustomUserDetails(savedAdmin);
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .role(savedAdmin.getRole().name())
                .username(savedAdmin.getName())
                .build();
    }
}
