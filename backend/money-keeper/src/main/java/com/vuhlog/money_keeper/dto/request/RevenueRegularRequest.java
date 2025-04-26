package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueRegularRequest {
    private Long amount;
    private String location;
    private String interpretation;
    private String revenueDate;
    private String dictionaryBucketPaymentId;
    private String dictionaryRevenueId;
    private String tripEvent;
    private String collectMoneyWho;
}
