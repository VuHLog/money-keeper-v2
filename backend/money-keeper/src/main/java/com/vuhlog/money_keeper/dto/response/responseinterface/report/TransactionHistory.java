package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface TransactionHistory {
    String getId();

    String getTransactionType();

    Long getAmount();

    Long getConvertedAmount();

    String getCurrencySymbol();

    String getCurrency();

    String getCategoryName();

    String getCategoryIconUrl();

    String getAccountName();

    String getBucketPaymentIconUrl();

    String getDate();

    String getTransferType();

    String getInterpretation();

    String getTripEvent();

    String getLocation();

    String getBeneficiary();

    String getBeneficiaryAccountName();

    String getBeneficiaryAccountIconUrl();

    String getCollectMoneyWho();

    String getSenderAccountName();

    String getSenderAccountIconUrl();

    String getFinancialGoalName();
}
