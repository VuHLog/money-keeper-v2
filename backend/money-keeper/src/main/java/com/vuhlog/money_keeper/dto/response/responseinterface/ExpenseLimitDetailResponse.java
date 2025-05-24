package com.vuhlog.money_keeper.dto.response.responseinterface;

public interface ExpenseLimitDetailResponse {
    String getName();
    Long getAmount();
    Long getConvertedAmount();
    String getCurrency();
    String getCurrencySymbol();
    String getBucketPaymentName();
    String getIconUrl();
    String getExpenseDate();
}
