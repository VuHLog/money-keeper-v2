package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialGoalResponse {
    private String id;
    private String name;
    private Long targetAmount;
    private Long currentAmount;
    private String deadline;
    private UserResponse user;
    private DictionaryBucketPaymentResponse bucketPayment;
    private String interpretation;
    private String currency;
    private String currencySymbol;
    private Integer status;
    private String createdAt;
    private String updatedAt;
}
