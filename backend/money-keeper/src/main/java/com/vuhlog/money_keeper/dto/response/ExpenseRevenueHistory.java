package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseRevenueHistory {
    private String id;
    private long currentBalance;
    private String date;
    private long amount;
    private String iconUrl;
    private String categoryName;
    private String type;
    private String interpretation;
    private String transferType;
    private String accountName;
}
