package com.vuhlog.money_keeper.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportBucketPaymentTypeBalanceDTO {
    Long totalBalance;
    String accountType;
    String iconUrl;
}
