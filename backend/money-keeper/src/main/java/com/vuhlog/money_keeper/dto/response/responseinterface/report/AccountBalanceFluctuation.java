package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface AccountBalanceFluctuation {
    String getTime();
    String getBucketPaymentId();
    String getAccountName();
    Long getBalanceAfterTransaction();
    Long getCurrentBalance();
}
