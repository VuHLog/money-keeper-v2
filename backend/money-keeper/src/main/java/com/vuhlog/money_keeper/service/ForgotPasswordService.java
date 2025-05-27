package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.entity.ForgotPassword;
import com.vuhlog.money_keeper.entity.Users;
import jakarta.mail.MessagingException;

public interface ForgotPasswordService {
    void sendOtp(Users user) throws MessagingException;

    Boolean verifyOtp(Integer otp, Users user);
}
