package com.vuhlog.money_keeper.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportBucketPaymentBalanceDTO {
    private String id;
    private String accountName;
    private String accountType;
    private Long balance;
    private Long initialBalance;
    private String iconUrl;
    private String currency;
    private String currencySymbol;
}
