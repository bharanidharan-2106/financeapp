package com.financeapp.service;

import com.financeapp.dto.RoleUpdateRequest;
import com.financeapp.dto.StatusUpdateRequest;
import com.financeapp.dto.UserRequest;
import com.financeapp.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest userRequest);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUserRole(Long id, RoleUpdateRequest roleUpdateRequest);

    UserResponse updateUserStatus(Long id, StatusUpdateRequest statusUpdateRequest);
}
