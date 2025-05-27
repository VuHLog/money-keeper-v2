package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.model.MailStructure;
import jakarta.mail.MessagingException;

public interface MailService {
    void sendEmail(String mail, MailStructure mailStructure) throws MessagingException;
}
