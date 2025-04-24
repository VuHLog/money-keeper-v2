package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseLimitRequest {
    private Long amount;
    private String name;
    private String categoriesId;
    private String bucketPaymentIds;
    private String repeatTime;
    private String startDate;
    private String endDate;
    private String startDateLimit;
    private String endDateLimit;
}
