package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRevenueSituation {
    private String bucketPaymentIds;
    private String timeOption;
    private Integer year;
    private Integer startYear;
    private Integer endYear;
    private String startDate;
    private String endDate;
}
