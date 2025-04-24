package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalExpenseRevenueRequest {
    private List<String> bucketPaymentIds;
    private List<String> categoriesId;
    private String timeOption;
    private String startDate;
    private String endDate;
    private Boolean isSelectAllBucketPayment;
}
