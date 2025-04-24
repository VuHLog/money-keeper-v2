package com.vuhlog.money_keeper.common;

import com.vuhlog.money_keeper.dao.UsersRepository;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommon {
    private final UsersRepository usersRepository;
    public Users getMyUserInfo(){
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
