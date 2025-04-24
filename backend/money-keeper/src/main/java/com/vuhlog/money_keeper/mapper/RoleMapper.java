package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.response.RoleResponse;
import com.vuhlog.money_keeper.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleResponse toRoleResponse(Role role);
}
