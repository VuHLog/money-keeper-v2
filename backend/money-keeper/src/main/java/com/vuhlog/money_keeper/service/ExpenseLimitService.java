package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ExpenseLimitRequest;
import com.vuhlog.money_keeper.dto.request.ExpenseLimitSearchRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitDetailResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ExpenseLimitService {
    ExpenseLimitResponse getExpenseLimitById(String id);
    List<ExpenseLimitDetailResponse> getExpenseLimitDetailById(String id, String startDate, String endDate);
    List<ExpenseLimitResponse> getAllExpenseLimit(ExpenseLimitSearchRequest req);
    Page<ExpenseLimitResponse> getAllExpenseLimitPagination(ExpenseLimitSearchRequest req);
    ExpenseLimitResponse createExpenseLimit(ExpenseLimitRequest request);
    ExpenseLimitResponse updateExpenseLimit(String id, ExpenseLimitRequest request);
    void deleteExpenseLimit(String id);

}
