package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.FinancialGoalRequest;
import com.vuhlog.money_keeper.dto.response.FinancialGoalResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FinancialGoalService {
    FinancialGoalResponse getFinancialGoalById(String id);
    List<FinancialGoalResponse> getAllFinancialGoal();
    Page<FinancialGoalResponse> getAllFinancialGoalPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search, Integer status);
    FinancialGoalResponse createFinancialGoal(FinancialGoalRequest request, String userId);
    FinancialGoalResponse updateFinancialGoal(String id, FinancialGoalRequest request);
    void deleteFinancialGoal(String id);
}
