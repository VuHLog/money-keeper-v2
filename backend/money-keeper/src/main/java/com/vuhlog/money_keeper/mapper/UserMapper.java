package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.UserCreationRequest;
import com.vuhlog.money_keeper.dto.request.UserUpdateRequest;
import com.vuhlog.money_keeper.dto.response.UserResponse;
import com.vuhlog.money_keeper.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "user_roles",ignore = true)
    Users toUser(UserCreationRequest request);

    UserResponse toUserResponse(Users user);

    void updateUser(@MappingTarget Users user, UserUpdateRequest request);
}
