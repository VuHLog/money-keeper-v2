package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportTransactionTypeReponse {
    Long getTotalExpense();
    Long getTotalRevenue();
    String getTime();
    Long getTotalTransaction();
}
