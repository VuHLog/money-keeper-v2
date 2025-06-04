package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import com.vuhlog.money_keeper.entity.ExpenseRegular;
import com.vuhlog.money_keeper.entity.FinancialGoal;
import com.vuhlog.money_keeper.entity.RevenueRegular;
import org.springframework.data.domain.Page;

import java.util.List;

public interface NotificationService {
    Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize, Integer readStatus);

    NotificationResponse revenueNotification(RevenueRegular revenueRegular, Boolean isCreate);

    NotificationResponse expenseNotification(ExpenseRegular expenseRegular, Boolean isCreate);

    NotificationResponse expenseForGoalNotification(ExpenseRegular expenseRegular);

    NotificationResponse financialGoalDeadlineExpired(FinancialGoal financialGoal);

    NotificationResponse deleteRevenueNotification(RevenueRegular revenueRegular);

    NotificationResponse deleteExpenseNotification(ExpenseRegular expenseRegular);

    NotificationResponse expenseTransferNotification(ExpenseRegular expenseRegular);

    Long countAllNotificationsByReadStatus(Integer readStatus);

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse expenseLimitNotification(List<ExpenseLimitNotification> expenseLimitNotifications);

    String updateReadStatus(String id, Integer readStatus);

    String updateAllReadStatus(Integer readStatus);

    void deleteNotification(String id);
}
