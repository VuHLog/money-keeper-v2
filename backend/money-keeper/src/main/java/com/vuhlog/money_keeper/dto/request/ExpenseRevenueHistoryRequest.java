package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRevenueHistoryRequest {
    private String bucketPaymentId;
    private String timeOption;
    private String startDate;
    private String endDate;
}
