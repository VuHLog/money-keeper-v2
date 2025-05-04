package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportExpenseCategory {
    Long getTotalExpense();
    String getCategoryId();
    String getCategoryName();
    String getIconUrl();
    String getTime();
}
