package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ChangePasswordRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.service.ForgotPasswordService;
import com.vuhlog.money_keeper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordController {
    private final UserService userService;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/verify-mail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable  String email) {
        Users user = userService.getUserByEmail(email, false);
        if(user == null) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }

        try {
            forgotPasswordService.sendOtp(user);
        } catch (Exception e) {
            throw new AppException(ErrorCode.EMAIL_SENDING_FAILED);
        }
        return ResponseEntity.ok("Email sent for verification. Please check your inbox.");
    }

    @PostMapping("/verify-otp/{otp}/{email}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String email) {
        Users user = userService.getUserByEmail(email, false);
        if(user == null) {
            throw new AppException(ErrorCode.INVALID_EMAIL);
        }

        if(forgotPasswordService.verifyOtp(otp, user)) {
            return ResponseEntity.ok("OTP verified successfully.");
        } else {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
    }

    @PostMapping("/change-password/{email}")
    public ApiResponse<String> changePassword(@PathVariable String email, @RequestBody ChangePasswordRequest request) {
        return ApiResponse.<String>builder()
                .result(userService.changePasswordForgotPassword(request, email))
                .build();
    }
}
