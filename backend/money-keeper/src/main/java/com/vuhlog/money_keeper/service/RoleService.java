package com.vuhlog.money_keeper.service;


import com.vuhlog.money_keeper.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    public List<RoleResponse> getRoles();

    public RoleResponse getById(String id);
}
