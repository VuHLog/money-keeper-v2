package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ExpenseLimitRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitDetailResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import com.vuhlog.money_keeper.service.ExpenseLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense-limit")
@RequiredArgsConstructor
public class ExpenseLimitController {
    private final ExpenseLimitService expenseLimitService;

    @GetMapping("")
    public ApiResponse<List<ExpenseLimitResponse>> getAllExpenseLimit() {
        return ApiResponse.<List<ExpenseLimitResponse>>builder()
                .result(expenseLimitService.getAllExpenseLimit())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ExpenseLimitResponse> getExpenseLimitById(@PathVariable String id) {
        return ApiResponse.<ExpenseLimitResponse>builder()
                .result(expenseLimitService.getExpenseLimitById(id))
                .build();
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<List<ExpenseLimitDetailResponse>> getExpenseLimitDetailById(
            @PathVariable String id,
            @RequestParam(name = "startDate", required = true) String startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "") String endDate
    ) {
        return ApiResponse.<List<ExpenseLimitDetailResponse>>builder()
                .result(expenseLimitService.getExpenseLimitDetailById(id, startDate, endDate))
                .build();
    }

    @PostMapping()
    public ApiResponse<ExpenseLimitResponse> createExpenseLimit(@RequestBody ExpenseLimitRequest request) {
        return ApiResponse.<ExpenseLimitResponse>builder()
                .result(expenseLimitService.createExpenseLimit(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ExpenseLimitResponse> updateExpenseLimit(@PathVariable String id, @RequestBody ExpenseLimitRequest request) {
        return ApiResponse.<ExpenseLimitResponse>builder()
                .result(expenseLimitService.updateExpenseLimit(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteExpenseLimit(@PathVariable String id) {
        expenseLimitService.deleteExpenseLimit(id);
        return ApiResponse.<String>builder()
                .result("Delete expense limit successfully")
                .build();
    }
}
