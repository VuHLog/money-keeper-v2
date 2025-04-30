package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PeriodOfTimeRequest {
    private String bucketPaymentIds;
    private String categoriesId;
    private String startDate;
    private String endDate;
}
