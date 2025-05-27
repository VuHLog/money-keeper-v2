package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.dao.ForgotPasswordRepository;
import com.vuhlog.money_keeper.entity.ForgotPassword;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.model.MailStructure;
import com.vuhlog.money_keeper.service.ForgotPasswordService;
import com.vuhlog.money_keeper.service.MailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgotPasswordImpl implements ForgotPasswordService {
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final MailService mailService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendOtp(Users user) throws MessagingException {
        int otp = otpGenerator();
        MailStructure mailStructure = MailStructure.builder()
                .subject("OTP yêu cầu quên mật khẩu")
                .message("Đây là mã OTP của bạn: <b>" + otp + "</b>. Mã này sẽ hết hạn sau 60 giây.")
                .build();
        ForgotPassword forgotPassword = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();
        mailService.sendEmail(user.getEmail(), mailStructure);
        forgotPasswordRepository.findByUser_id(user.getId()).ifPresent(forgotPassword1 -> forgotPassword.setId(forgotPassword1.getId()));
        forgotPasswordRepository.save(forgotPassword);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean verifyOtp(Integer otp, Users user) {
        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser_id(otp, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP_EMAIL));

        if(forgotPassword.getExpirationTime().before(Date.from(Instant.now()))){
            forgotPasswordRepository.deleteById(forgotPassword.getId());
            return false;
        }
        return true;
    }

    private Integer otpGenerator(){
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }
}
