package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TotalExpenseRevenueForCategoryRequest {
    private String timeOption;
    private String presentOption;
    private String bucketPaymentIds;
    private Integer month;
    private Integer quarter;
    private Integer year;
    private String startDate;
    private String endDate;
}
