package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportTotalBucketPayment {
    Long getTotalInitialBalance();

    Long getTotalExpense();

    Long getTotalRevenue();

    Long getTotalBalance();
}
