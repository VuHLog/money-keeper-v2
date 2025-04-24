package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ChangePasswordRequest;
import com.vuhlog.money_keeper.dto.request.UserCreationRequest;
import com.vuhlog.money_keeper.dto.request.UserUpdateRequest;
import com.vuhlog.money_keeper.dto.response.UserResponse;
import com.vuhlog.money_keeper.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    public Page<UserResponse> getUsers(Pageable pageable);

    public Page<UserResponse> getUsersContains(String s,Pageable pageable);
    public UserResponse getById(String id);

    Users getMyUserInfo();

    public UserResponse getByUsername(String username);

    String changePassword(ChangePasswordRequest request);

    public UserResponse addUser(UserCreationRequest request);

    public UserResponse updateUser(String userId, UserUpdateRequest request);

    public void deleteUser(String userId);

    public UserResponse getMyInfo();
}
