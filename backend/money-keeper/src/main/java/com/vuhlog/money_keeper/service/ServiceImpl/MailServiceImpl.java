package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.model.MailStructure;
import com.vuhlog.money_keeper.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Override
    public void sendEmail(String mail, MailStructure mailStructure) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);;

        try {
            helper.setFrom(fromMail);
            helper.setTo(mail);
            helper.setSubject(mailStructure.getSubject());
            helper.setText(mailStructure.getMessage(), true); // true để chỉ định rằng đây là HTML
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace(); // Xử lý lỗi nếu cần
        }
    }
}
