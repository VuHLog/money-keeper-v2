package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryResponse {
    private String id;
    private String transactionId;
    private String transactionType;
    private long oldAmount;
    private long newAmount;
    private String oldCategoryId;
    private String newCategoryId;
    private String beneficiaryAccountId;
    private String senderAccountId;
    private String updatedAt;
    private DictionaryBucketPaymentResponse bucketPayment;
}
