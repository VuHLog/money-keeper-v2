package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize);

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse expenseLimitNotification(List<ExpenseLimitNotification> expenseLimitNotifications);

    String updateReadStatus(String id, Integer readStatus);

    void deleteNotification(String id);
}
