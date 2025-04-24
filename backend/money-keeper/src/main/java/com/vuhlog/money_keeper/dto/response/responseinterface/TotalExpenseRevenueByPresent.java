package com.vuhlog.money_keeper.dto.response.responseinterface;

public interface TotalExpenseRevenueByPresent {
    Long getTodayExpense();
    Long getWeekExpense();
    Long getMonthExpense();
    Long getQuarterExpense();
    Long getYearExpense();
    Long getTodayRevenue();
    Long getWeekRevenue();
    Long getMonthRevenue();
    Long getQuarterRevenue();
    Long getYearRevenue();
}
