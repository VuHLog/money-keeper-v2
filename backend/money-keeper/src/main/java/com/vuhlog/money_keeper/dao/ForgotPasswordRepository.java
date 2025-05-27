package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, String> {
  Optional<ForgotPassword> findByOtpAndUser_id(Integer otp, String userId);
  Optional<ForgotPassword> findByUser_id(String userId);
}