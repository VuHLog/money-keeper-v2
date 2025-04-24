package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.RoleRepository;
import com.vuhlog.money_keeper.dto.response.RoleResponse;
import com.vuhlog.money_keeper.mapper.RoleMapper;
import com.vuhlog.money_keeper.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public RoleResponse getById(String id) {
        return roleMapper.toRoleResponse(roleRepository.findById(id).get());
    }

    @Override
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

}
