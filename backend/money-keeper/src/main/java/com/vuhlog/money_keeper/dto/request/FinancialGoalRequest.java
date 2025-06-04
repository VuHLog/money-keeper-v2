package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialGoalRequest {
    private String name;
    private Long targetAmount;
    private String deadline;
    private String bucketPaymentId;
    private String interpretation;
}
