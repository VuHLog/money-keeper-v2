package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.ExpenseRegularRequest;
import com.vuhlog.money_keeper.dto.request.FinancialGoalRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRegularResponse;
import com.vuhlog.money_keeper.dto.response.FinancialGoalResponse;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.service.ExpenseRegularService;
import com.vuhlog.money_keeper.service.FinancialGoalService;
import com.vuhlog.money_keeper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/financial-goal")
@RequiredArgsConstructor
public class FinancialGoalController {
    private final FinancialGoalService financialGoalService;
    private final ExpenseRegularService expenseRegularService;
    private final UserService userService;

    @GetMapping("/pagination")
    public ApiResponse<Page<FinancialGoalResponse>> getAllFinancialGoals(
            @RequestParam(name = "field", defaultValue = "name") String field,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "5") Integer pageSize,
            @RequestParam(name = "sort", defaultValue = "ASC") String sort,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "status", defaultValue = "0") Integer status
            ) {
        return ApiResponse.<Page<FinancialGoalResponse>>builder()
                .result(financialGoalService.getAllFinancialGoalPagination(field, pageNumber, pageSize, sort, search,status))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<FinancialGoalResponse> getFinancialGoalById(
            @PathVariable String id
    ){
        return ApiResponse.<FinancialGoalResponse>builder()
                .result(financialGoalService.getFinancialGoalById(id))
                .build();
    }

    @GetMapping("/{id}/diposit-history")
    public ApiResponse<Page<ExpenseRegularResponse>> getDipositHistory(
            @PathVariable String id,
            @RequestParam(name = "field", defaultValue = "expenseDate") String field,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "2") Integer pageSize,
            @RequestParam(name = "sort", defaultValue = "DESC") String sort,
            @RequestParam(name = "search", defaultValue = "") String search
    ){
        return ApiResponse.<Page<ExpenseRegularResponse>>builder()
                .result(expenseRegularService.getByFinancialGoalPagination(field, pageNumber, pageSize, sort, search, id))
                .build();
    }

    @PostMapping("")
    public ApiResponse<FinancialGoalResponse> createFinancialGoal(
            @RequestBody FinancialGoalRequest request
    ){
        Users user = userService.getMyUserInfo();
        return ApiResponse.<FinancialGoalResponse>builder()
                .result(financialGoalService.createFinancialGoal(request, user.getId()))
                .build();
    }

    @PostMapping("/{id}/deposit")
    public ApiResponse<ExpenseRegularResponse> createDeposit(
            @RequestBody ExpenseRegularRequest request
    ){
        Users user = userService.getMyUserInfo();
        return ApiResponse.<ExpenseRegularResponse>builder()
                .result(expenseRegularService.createExpenseRegularForGoal(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<FinancialGoalResponse> updateFinancialGoal(
            @PathVariable String id,
            @RequestBody FinancialGoalRequest request
    ){
        return ApiResponse.<FinancialGoalResponse>builder()
                .result(financialGoalService.updateFinancialGoal(id, request))
                .build();
    }

    @PutMapping("/{id}/deadline")
    public ApiResponse<FinancialGoalResponse> updateDeadlineFinancialGoal(
            @PathVariable String id,
            @RequestBody FinancialGoalRequest request
    ){
        return ApiResponse.<FinancialGoalResponse>builder()
                .result(financialGoalService.updateDeadlineFinancialGoal(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> deleteFinancialGoal(
            @PathVariable String id
    ){
        List<ExpenseRegularResponse> expenseRegularResponseList = expenseRegularService.getByFinancialGoal(id);
        for(ExpenseRegularResponse expenseRegular : expenseRegularResponseList){
            expenseRegularService.deleteExpenseRegularNoNotify(expenseRegular.getId());
        }
        financialGoalService.deleteFinancialGoal(id);
        return ApiResponse.<String>builder()
                .result("delete financial goal is successfully")
                .build();
    }
}
