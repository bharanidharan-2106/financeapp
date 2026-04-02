package com.financeapp.service.impl;

import com.financeapp.dto.RoleUpdateRequest;
import com.financeapp.dto.StatusUpdateRequest;
import com.financeapp.dto.UserRequest;
import com.financeapp.dto.UserResponse;
import com.financeapp.entity.Status;
import com.financeapp.entity.User;
import com.financeapp.exception.DuplicateEmailException;
import com.financeapp.exception.UserNotFoundException;
import com.financeapp.repository.UserRepository;
import com.financeapp.service.UserService;
import com.financeapp.util.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateEmailException(userRequest.getEmail());
        }

        User user = User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password(passwordEncoder.encode(userRequest.getPassword()))
                .role(userRequest.getRole())
                .status(Status.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserMapper.toResponse(user);
    }

    @Override
    public UserResponse updateUserRole(Long id, RoleUpdateRequest roleUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setRole(roleUpdateRequest.getRole());
        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse updateUserStatus(Long id, StatusUpdateRequest statusUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setStatus(statusUpdateRequest.getStatus());
        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }
}
