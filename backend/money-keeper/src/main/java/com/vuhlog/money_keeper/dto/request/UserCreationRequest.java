package com.vuhlog.money_keeper.dto.request;

import com.vuhlog.money_keeper.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationRequest {
    private String fullName;

    private String username;

    private String password;

    private String email;

    private String avatarUrl;

    private Set<Role> roles;
}
