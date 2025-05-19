package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportBucketPayment {
    String getId();

String getAccountName();

    String getAccountType();

    Long getInitialBalance();

    Long getTotalExpense();

    Long getTotalRevenue();

    Long getBalance();

    Long getDisparity();
}
