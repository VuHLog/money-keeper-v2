package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ExpenseRevenueHistoryRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRevenueHistory;
import com.vuhlog.money_keeper.service.DictionaryBucketPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final DictionaryBucketPaymentService dictionaryBucketPaymentService;

    @GetMapping("/total-expense")
    public ApiResponse<Long> getTotalExpense(
            ExpenseRevenueHistoryRequest req) {
        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getTotalExpenseByBucketPaymentId(req))
                .build();
    }

    @GetMapping("/total-revenue")
    public ApiResponse<Long> getTotalRevenue(
            ExpenseRevenueHistoryRequest req) {

        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getTotalRevenueByBucketPaymentId(req))
                .build();
    }

    @GetMapping("/history")
    public ApiResponse<List<ExpenseRevenueHistory>> getTransactionHistory(
            @RequestParam(name = "field", required = false, defaultValue = "startDate") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort,
            ExpenseRevenueHistoryRequest req) {
        return ApiResponse.<List<ExpenseRevenueHistory>>builder()
                .result(dictionaryBucketPaymentService.getAllExpenseRevenueRegularsByIdAndDate(
                        req, pageNumber, pageSize, sort))
                .build();
    }
}
