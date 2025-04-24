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
    private long balance;
    private String location;
    private String interpretation;
    private String revenueDate;
    private String createdDate;
    private String modifiedDate;
    private DictionaryBucketPaymentResponse dictionaryBucketPayment;
    private DictionaryBucketPaymentResponse senderAccount;
    private DictionaryRevenue dictionaryRevenue;
    private TripEventResponse tripEvent;
    private CollectMoneyWhoResponse collectMoneyWho;
    private String expenseRegularId;
}
