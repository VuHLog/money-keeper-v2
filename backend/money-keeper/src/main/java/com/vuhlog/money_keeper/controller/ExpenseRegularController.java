package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ExpenseRegularRequest;
import com.vuhlog.money_keeper.dto.request.TransferRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRegularResponse;
import com.vuhlog.money_keeper.dto.response.RevenueRegularResponse;
import com.vuhlog.money_keeper.dto.response.TotalExpenseByDateResponse;
import com.vuhlog.money_keeper.service.ExpenseRegularService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense-regular")
@RequiredArgsConstructor
public class ExpenseRegularController {
    private final ExpenseRegularService expenseRegularService;

    @GetMapping("")
    public ApiResponse<List<ExpenseRegularResponse>> getAllMyExpenseRegular(@RequestParam(name = "dictionaryBucketPaymentId", required = true) String dictionaryBucketPaymentId) {
        return ApiResponse.<List<ExpenseRegularResponse>>builder().result(expenseRegularService.getAllMyExpenseRegular(dictionaryBucketPaymentId)).build();
    }

    @GetMapping("/pagination")
    public ApiResponse<Page<ExpenseRegularResponse>> getAllMyExpenseRegularPagination(
            @RequestParam(name = "field", required = false, defaultValue = "modifiedDate") String field,
            @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", required = false, defaultValue = "desc") String sort,
            @RequestParam(name = "search", required = false, defaultValue = "") String search
    ) {
        return ApiResponse.<Page<ExpenseRegularResponse>>builder().result(expenseRegularService.getAllMyExpenseRegularPagination(field, pageNumber, pageSize, sort, search)).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseRegularResponse> getExpenseRegularById(@PathVariable String id) {
        return ApiResponse.<ExpenseRegularResponse>builder().result(expenseRegularService.getExpenseRegularById(id)).build();
    }

    @GetMapping("/total-by-expense-limit")
    public ApiResponse<List<TotalExpenseByDateResponse>> getTotalExpenseByExpenseLimit(
            @RequestParam(name = "expenseLimitId", required = true) String expenseLimitId,
            @RequestParam(name = "startDate", required = true) String startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "") String endDate
    ) {
        return ApiResponse.<List<TotalExpenseByDateResponse>>builder().result(expenseRegularService.getTotalExpenseByExpenseLimit(expenseLimitId, startDate, endDate)).build();
    }

    @PostMapping("")
    public ApiResponse<ExpenseRegularResponse> createExpenseRegular(@RequestBody ExpenseRegularRequest req) {
        return ApiResponse.<ExpenseRegularResponse>builder().result(expenseRegularService.createExpenseRegular(req)).build();
    }

    @PostMapping("/transfer")
    public ApiResponse<ExpenseRegularResponse> createTransfer(@RequestBody TransferRequest req) {
        return ApiResponse.<ExpenseRegularResponse>builder().result(expenseRegularService.createExpenseRegularFromTransferRequest(req)).build();
    }

//    @PutMapping("/{id}/transfer")
//    public ApiResponse<ExpenseRegularResponse> updateTransfer(@PathVariable String id, @RequestBody TransferRequest req) {
//        return ApiResponse.<ExpenseRegularResponse>builder().result(expenseRegularService.updateExpenseRegularFromTransferRequest(id, req)).build();
//    }

    @PutMapping("/{id}")
    public ApiResponse<ExpenseRegularResponse> updateExpenseRegular(@PathVariable String id, @RequestBody ExpenseRegularRequest req) {
        return ApiResponse.<ExpenseRegularResponse>builder().result(expenseRegularService.updateExpenseRegular(id, req)).build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteExpenseRegularById(@PathVariable String id) {
        expenseRegularService.deleteExpenseRegular(id);
        return ApiResponse.<String>builder().result("delete expense regular successfully").build();
    }

    @RequestMapping(value = "/transfer/{id}", method = RequestMethod.DELETE)
    public ApiResponse<String> deleteExpenseRegularByIdAndTransferType(@PathVariable String id) {
        expenseRegularService.deleteExpenseRegularByTransferType(id);
        return ApiResponse.<String>builder().result("delete expense regular is transfer type successfully").build();
    }
}
