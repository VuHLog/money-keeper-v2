package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExpenseRevenueResponse {
    private String id;
    private int month;
    private int year;
    private long totalExpense;
    private long totalRevenue;
    private String createdAt;
    private String updatedAt;
    private String bucketPaymentId;
    private String userId;
    private String categoryId;
    private String type;
}
