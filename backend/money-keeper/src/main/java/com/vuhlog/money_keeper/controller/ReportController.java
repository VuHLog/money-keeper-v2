package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportExpenseCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportRevenueCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;
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
}
