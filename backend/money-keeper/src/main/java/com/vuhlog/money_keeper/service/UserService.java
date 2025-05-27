package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ChangePasswordRequest;
import com.vuhlog.money_keeper.dto.request.UserCreationRequest;
import com.vuhlog.money_keeper.dto.request.UserUpdateRequest;
import com.vuhlog.money_keeper.dto.response.UserResponse;
import com.vuhlog.money_keeper.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;

public interface UserService {
    Page<UserResponse> getUsers(Pageable pageable);

    Page<UserResponse> getUsersContains(String s,Pageable pageable);

    UserResponse getById(String id);

    Users getMyUserInfo();

    Users getUserByEmail(String email, Boolean oauth2);

    UserResponse getByUsername(String username);

    String changePassword(ChangePasswordRequest request);

    String changePasswordForgotPassword(ChangePasswordRequest request, String email);

    UserResponse addUser(UserCreationRequest request) throws IOException;

    UserResponse updateUser(String userId, UserUpdateRequest request);

    void deleteUser(String userId);

    UserResponse getMyInfo();

    void executeSqlScriptForUser(String userId) throws IOException;
}
