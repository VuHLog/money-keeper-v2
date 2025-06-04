package com.vuhlog.money_keeper.controller;

import java.util.List;

import com.vuhlog.money_keeper.dto.request.BucketPaymentUsageStatus;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.vuhlog.money_keeper.dto.request.DictionaryBucketPaymentRequest;
import com.vuhlog.money_keeper.dto.request.ExpenseRevenueHistoryRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.DictionaryBucketPaymentResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRevenueHistory;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.service.DictionaryBucketPaymentService;
import com.vuhlog.money_keeper.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dictionary-bucket-payment")
@RequiredArgsConstructor
public class DictionaryBucketPaymentController {
    private final DictionaryBucketPaymentService dictionaryBucketPaymentService;
    private final UserService usersService;

    @GetMapping("")
    public ApiResponse<List<DictionaryBucketPaymentResponse>> getAllDictionaryBucketPayment(@RequestParam(name = "search", required = false, defaultValue = "") String search) {
        Users user = usersService.getMyUserInfo();
        return ApiResponse.<List<DictionaryBucketPaymentResponse>>builder()
                .result(dictionaryBucketPaymentService.getAllDictionaryBucketPayment(user.getId(), search))
                .build();
    }

    @GetMapping("/pagination")
    public ApiResponse<Page<DictionaryBucketPaymentResponse>> getDictionaryBucketPaymentByPagination(
            @RequestParam(name = "field", required = false, defaultValue = "accountName") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "ASC") String sort,
            @RequestParam(name = "search", required = false, defaultValue = "") String search
    ) {
        Users user = usersService.getMyUserInfo();
        return ApiResponse.<Page<DictionaryBucketPaymentResponse>>builder()
                .result(dictionaryBucketPaymentService.getDictionaryBucketPaymentPagination(user.getId(),field, pageNumber, pageSize, sort, search))
                .build();
    }

    @GetMapping("/total-balance")
    public ApiResponse<Long> getMyTotalBalance(@RequestParam(name = "search", required = false, defaultValue = "") String search) {
        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getTotalBalanceBySearch(search))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<DictionaryBucketPaymentResponse> getDictionaryBucketPaymentById(
            @PathVariable(name = "id") String id) {
        return ApiResponse.<DictionaryBucketPaymentResponse>builder()
                .result(dictionaryBucketPaymentService.getDictionaryBucketPaymentById(id))
                .build();
    }

    @GetMapping("/{id}/total-expense")
    public ApiResponse<Long> getTotalExpenseById(
            @PathVariable(name = "id") String id,
            ExpenseRevenueHistoryRequest req) {
        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getTotalExpenseByBucketPaymentId(req))
                .build();
    }

    @GetMapping("/{id}/total-revenue")
    public ApiResponse<Long> getTotalRevenueById(
            @PathVariable(name = "id") String id,
            ExpenseRevenueHistoryRequest req) {

        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getTotalRevenueByBucketPaymentId(req))
                .build();
    }

    @GetMapping("/{id}/balance")
    public ApiResponse<Long> getAmountById(@PathVariable(name = "id") String id) {
        return ApiResponse.<Long>builder()
                .result(dictionaryBucketPaymentService.getBalanceByBucketPaymentId(id))
                .build();
    }

    @GetMapping("/{id}/transaction-history")
    public ApiResponse<List<ExpenseRevenueHistory>> getTransactionHistory(
            @PathVariable(name = "id") String id,
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

    @PostMapping("")
    public ApiResponse<DictionaryBucketPaymentResponse> createDictionaryBucketPayment(
            @RequestBody DictionaryBucketPaymentRequest request) {
        Users user = usersService.getMyUserInfo();
        return ApiResponse.<DictionaryBucketPaymentResponse>builder()
                .result(dictionaryBucketPaymentService.createDictionaryBucketPayment(request, user.getId()))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<DictionaryBucketPaymentResponse> updateDictionaryBucketPayment(
            @PathVariable(name = "id") String id,
            @RequestBody DictionaryBucketPaymentRequest request) {
        return ApiResponse.<DictionaryBucketPaymentResponse>builder()
                .result(dictionaryBucketPaymentService.updateDictionaryBucketPayment(id, request))
                .build();
    }

    @PatchMapping("/{id}/usage-status")
    public ApiResponse<DictionaryBucketPaymentResponse> updateUsageStatus(
            @PathVariable(name = "id") String id,
            @RequestBody BucketPaymentUsageStatus request) {
        return ApiResponse.<DictionaryBucketPaymentResponse>builder()
                .result(dictionaryBucketPaymentService.updateUsageStatus(id, request))
                .build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteDictionaryBucketPayment(
            @PathVariable(name = "id") String id) {
        dictionaryBucketPaymentService.deleteDictionaryBucketPayment(id);
        return ApiResponse.<String>builder()
                .result("Delete bucket payment successfully")
                .build();
    }
}
