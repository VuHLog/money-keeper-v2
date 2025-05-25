package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportBucketPaymentBalance {
    String getId();
    String getAccountName();
    String getAccountType();
    Long getBalance();
    Long getInitialBalance();
    String getIconUrl();
    String getCurrency();
    String getCurrencySymbol();
}
