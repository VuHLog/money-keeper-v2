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
public class UserUpdateRequest {
    private String fullName;

    private String email;

    private String avatarUrl;
}
