package com.vuhlog.money_keeper.dto.response.responseinterface;

public interface ExpenseLimitNotification {
    String getId();
    String getName();
    Long getLimitAmount();
    Long getTotalExpense();
}
