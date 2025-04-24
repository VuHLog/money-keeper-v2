package com.vuhlog.money_keeper.dto.response.responseinterface;

public interface TotalExpenseRevenueForCategory {
    Long getTotalExpense();
    Long getTotalRevenue();
    String getType();
    String getName();
    String getIconUrl();
}
