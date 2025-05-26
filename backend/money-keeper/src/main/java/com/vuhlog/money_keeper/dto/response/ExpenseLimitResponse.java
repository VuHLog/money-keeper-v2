package com.vuhlog.money_keeper.dto.response;

import com.vuhlog.money_keeper.entity.DictionaryExpense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseLimitResponse {
    private String id;
    private Long amount;
    private String name;
    private List<DictionaryExpense> categories;
    private DictionaryBucketPaymentResponse bucketPayments;
    private String repeatTime;
    private String startDate;
    private String endDate;
    private String startDateLimit;
    private String endDateLimit;
    private Long spentAmount;
    private String currency;
    private String currencySymbol;
}
