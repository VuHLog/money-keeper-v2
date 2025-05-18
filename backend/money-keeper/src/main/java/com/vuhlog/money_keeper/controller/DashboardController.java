package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TotalExpenseRevenue;
import com.vuhlog.money_keeper.service.DictionaryBucketPaymentService;
import com.vuhlog.money_keeper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final ReportService reportService;
    private final DictionaryBucketPaymentService dictionaryBucketPaymentService;

    @GetMapping("/my-total-balance")
    public ApiResponse<Long> getMyTotalBalance() {
        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getMyTotalBalance())
                .build();
    }

    @GetMapping("/total-transaction-this-month")
    public ApiResponse<TotalExpenseRevenue> getTotalExpenseRevenueThisMonth() {
        return ApiResponse.<TotalExpenseRevenue>builder()
                .result(reportService.getTotalExpenseRevenueThisMonth())
                .build();
    }
}
