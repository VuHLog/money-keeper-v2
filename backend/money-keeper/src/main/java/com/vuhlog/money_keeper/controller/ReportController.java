package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.constants.TimeOptionExpenseRevenueSituationType;
import com.vuhlog.money_keeper.dto.request.ExpenseRevenueSituation;
import com.vuhlog.money_keeper.dto.request.ReportCategoryResponse;
import com.vuhlog.money_keeper.dto.request.TotalExpenseRevenueForCategoryRequest;
import com.vuhlog.money_keeper.dto.request.TotalExpenseRevenueRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.TotalExpenseRevenueResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseByExpenseLimit;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueForCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueForExpenseRevenueSituation;
import com.vuhlog.money_keeper.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/total-expense-revenue")
    public ApiResponse<TotalExpenseRevenueResponse> getTotalExpenseRevenueByTimeOption(TotalExpenseRevenueRequest req) {
        return ApiResponse.<TotalExpenseRevenueResponse>builder()
                .result(reportService.getTotalExpenseRevenueByTimeOption(req))
                .build();
    }

    @GetMapping("/total-expense-by-category")
    public ApiResponse<List<ReportCategoryResponse>> getTotalExpenseByTimeOptionAndCategory(TotalExpenseRevenueRequest req) {
        return ApiResponse.<List<ReportCategoryResponse>>builder()
                .result(reportService.getTotalExpenseByTimeOptionAndCategory(req))
                .build();
    }

    @GetMapping("/total-revenue-by-category")
    public ApiResponse<List<ReportCategoryResponse>> getTotalRevenueByTimeOptionAndCategory(TotalExpenseRevenueRequest req) {
        return ApiResponse.<List<ReportCategoryResponse>>builder()
                .result(reportService.getTotalRevenueByTimeOptionAndCategory(req))
                .build();
    }

    @GetMapping("/total-expense-by-expense-limit")
    public ApiResponse<TotalExpenseByExpenseLimit> getTotalExpenseGroupByExpenseLimit(
            @RequestParam(name = "expenseLimitId", required = true) String expenseLimitId,
            @RequestParam(name = "startDate", required = true) String startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "") String endDate
    ) {
        return ApiResponse.<TotalExpenseByExpenseLimit>builder()
                .result(reportService.getTotalExpenseByExpenseLimit(expenseLimitId, startDate, endDate))
                .build();
    }

    @PostMapping("/expense-revenue-situation")
    public <T> ApiResponse<T> getTotalExpenseRevenueForExpenseRevenueSituation(@RequestBody ExpenseRevenueSituation req) {
        T result = null;
        String timeOption = req.getTimeOption();
        if(timeOption.equals(TimeOptionExpenseRevenueSituationType.PRESENT.getType())){
            result = (T) reportService.getReportExpenseRevenueByPresent(req);
        } else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.OPTIONAL.getType())){
            result = (T) reportService.getReportExpenseRevenueByOptional(req);
        }else {
            result = (T) reportService.getTotalExpenseRevenueForExpenseRevenueSituation(req);
        }
        return ApiResponse.<T>builder()
                .result(result)
                .build();
    }

    @PostMapping("/expense-revenue-for-category")
    public ApiResponse<List<TotalExpenseRevenueForCategory>> getTotalExpenseRevenueForCategory(@RequestBody TotalExpenseRevenueForCategoryRequest req) {
        List<TotalExpenseRevenueForCategory> result = null;
        String timeOption = req.getTimeOption();
        if(timeOption.equals(TimeOptionExpenseRevenueSituationType.PRESENT.getType())){
            result = reportService.getTotalExpenseRevenueForCategoryByPresent(req);
        } else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.OPTIONAL.getType())){
            result = reportService.getTotalExpenseRevenueForCategoryByOptional(req);
        }else {
            result = reportService.getTotalExpenseRevenueForCategory(req);
        }
        return ApiResponse.<List<TotalExpenseRevenueForCategory>>builder()
                .result(result)
                .build();
    }
}
