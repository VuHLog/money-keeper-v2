package com.vuhlog.money_keeper.dto.response.responseinterface;

public interface ExpenseLimitDetailResponse {
    String getName();
    Long getAmount();
    String getBucketPaymentName();
    String getIconUrl();
    String getExpenseDate();
}
