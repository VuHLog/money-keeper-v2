package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportBucketPayment {
    String getId();

    String getAccountName();

    String getAccountType();

    Double getInitialBalance();

    String getCurrency();

    String getCurrencySymbol();

    Long getTotalExpense();

    Long getConvertedTotalExpense();

    Long getTotalRevenue();

    Long getConvertedTotalRevenue();

    Double getBalance();

    Long getDisparity();
}
