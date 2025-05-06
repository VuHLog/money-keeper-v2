package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface TransactionHistory {
    String getId();

    String getTransactionType();

    Long getAmount();

    String getCategoryName();

    String getCategoryIconUrl();

    String getAccountName();

    String getBucketPaymentIconUrl();

    String getDate();

    String getTransferType();

    String getInterpretation();
}
