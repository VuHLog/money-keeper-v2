package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRegularRequest {
    private Long amount;
    private String location;
    private String interpretation;
    private String expenseDate;
    private String dictionaryBucketPaymentId;
    private String financialGoalId;
    private String dictionaryExpenseId;
    private String tripEvent;
    private String beneficiary;
}
