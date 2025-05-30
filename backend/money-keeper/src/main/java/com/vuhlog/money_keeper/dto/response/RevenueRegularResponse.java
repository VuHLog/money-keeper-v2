package com.vuhlog.money_keeper.dto.response;

import com.vuhlog.money_keeper.entity.DictionaryRevenue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueRegularResponse {
    private String id;
    private Long amount;
    private String currency;
    private String currencySymbol;
    private Double exchangeRate;
    private Long convertedAmount;
    private Double balance;
    private Double convertedBalance;
    private String location;
    private String interpretation;
    private String revenueDate;
    private String transferType;
    private String createdDate;
    private String modifiedDate;
    private DictionaryBucketPaymentResponse dictionaryBucketPayment;
    private DictionaryBucketPaymentResponse senderAccount;
    private DictionaryRevenue dictionaryRevenue;
    private String tripEvent;
    private String collectMoneyWho;
    private String expenseRegularId;
}
