package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, String> {
    Role findByRoleName(String roleName);

}