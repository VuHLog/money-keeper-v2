package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryBucketPaymentResponse {
    private String id;
    private Double balance;
    private Double initialBalance;
    private Long creditLimit;
    private String accountName;
    private String accountType;
    private String interpretation;
    private boolean haveUse;
    private BankResponse bank;
    private String iconUrl;
    private String currency;
    private String currencySymbol;
}
