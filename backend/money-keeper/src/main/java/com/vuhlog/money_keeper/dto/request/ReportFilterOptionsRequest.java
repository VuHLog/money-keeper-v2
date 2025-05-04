package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportFilterOptionsRequest {
    private String timeOption;
    private String bucketPaymentIds;
    private String expenseCategoriesId;
    private String revenueCategoriesId;
    private List<String> customTimeRange;
    private String transactionType;
}
