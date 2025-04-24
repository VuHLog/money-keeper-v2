package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ExpenseRegularRequest;
import com.vuhlog.money_keeper.dto.request.TransferRequest;
import com.vuhlog.money_keeper.dto.response.ExpenseRegularResponse;
import com.vuhlog.money_keeper.dto.response.TotalExpenseByDateResponse;

import java.util.List;

public interface ExpenseRegularService {
    List<ExpenseRegularResponse> getAllMyExpenseRegular(String dictionaryBucketPaymentId);
    List<TotalExpenseByDateResponse> getTotalExpenseByExpenseLimit(String expenseLimitId, String startDate, String endDate);
    ExpenseRegularResponse createExpenseRegular(ExpenseRegularRequest request);
    ExpenseRegularResponse createExpenseRegularFromTransferRequest(TransferRequest request);
//    ExpenseRegularResponse updateExpenseRegularFromTransferRequest(String id, TransferRequest request);
    void deleteExpenseRegular(String id);
    void deleteExpenseRegularByTransferType(String id);
    ExpenseRegularResponse updateExpenseRegular(String id, ExpenseRegularRequest request);
    ExpenseRegularResponse getExpenseRegularById(String id);
}
