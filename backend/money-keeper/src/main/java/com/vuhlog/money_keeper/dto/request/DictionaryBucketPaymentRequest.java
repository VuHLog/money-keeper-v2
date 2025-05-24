package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryBucketPaymentRequest {
    private Double balance;
    private Long creditLimit;
    private String accountName;
    private String accountType;
    private String interpretation;
    private String bankId;
    private String iconUrl;
    private String currency;
    private String currencySymbol;
}
