package com.vuhlog.money_keeper.dto.response;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportTotalBucketPaymentDTO {
    Long totalInitialBalance;

    Long totalExpense;

    Long totalRevenue;

    Long totalBalance;
}
