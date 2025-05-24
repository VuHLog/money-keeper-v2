package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportBucketPayment {
    String getId();

    String getAccountName();

    String getAccountType();

    Double getInitialBalance();

    Long getTotalExpense();

    Long getTotalRevenue();

    Double getBalance();

    Long getDisparity();
}
