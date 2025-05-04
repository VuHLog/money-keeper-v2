package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import com.vuhlog.money_keeper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("transaction-type")
    public ApiResponse<List<ReportTransactionTypeReponse>> getReportTransactionType(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportTransactionTypeReponse>>builder()
                .result(reportService.getReportForTransactionType(request))
                .build();
    }

    @PostMapping("expense-category")
    public ApiResponse<List<ReportExpenseCategory>> getReportExpenseCategory(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportExpenseCategory>>builder()
                .result(reportService.getReportExpenseCategory(request))
                .build();
    }

    @PostMapping("revenue-category")
    public ApiResponse<List<ReportRevenueCategory>> getReportRevenueCategory(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportRevenueCategory>>builder()
                .result(reportService.getReportRevenueCategory(request))
                .build();
    }

    @PostMapping("daily-trend")
    public ApiResponse<List<ReportDailyTrend>> getReportDailyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportDailyTrend>>builder()
                .result(reportService.getReportDailyTrend(request))
                .build();
    }

    @PostMapping("weekly-trend")
    public ApiResponse<List<ReportWeeklyTrend>> getReportWeeklyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportWeeklyTrend>>builder()
                .result(reportService.getReportWeeklyTrend(request))
                .build();
    }

    @PostMapping("monthly-trend")
    public ApiResponse<List<ReportMonthlyTrend>> getReportMonthlyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportMonthlyTrend>>builder()
                .result(reportService.getReportMonthlyTrend(request))
                .build();
    }

    @PostMapping("yearly-trend")
    public ApiResponse<List<ReportYearlyTrend>> getReportYearlyTrend(@RequestBody ReportFilterOptionsRequest request) {
        return ApiResponse.<List<ReportYearlyTrend>>builder()
                .result(reportService.getReportYearlyTrend(request))
                .build();
    }

    @PostMapping("bucket-payment-balance")
    public ApiResponse<List<ReportBucketPaymentBalance>> getReportBucketPaymentBalance() {
        return ApiResponse.<List<ReportBucketPaymentBalance>>builder()
                .result(reportService.getReportBucketPaymentBalance())
                .build();
    }

    @PostMapping("bucket-payment-type-balance")
    public ApiResponse<List<ReportBucketPaymentTypeBalance>> getReportBucketPaymentTypeBalance() {
        return ApiResponse.<List<ReportBucketPaymentTypeBalance>>builder()
                .result(reportService.getReportBucketPaymentTypeBalance())
                .build();
    }
}
