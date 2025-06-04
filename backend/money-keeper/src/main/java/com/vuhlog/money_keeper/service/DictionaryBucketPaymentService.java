package com.vuhlog.money_keeper.service;

import java.util.List;

import com.vuhlog.money_keeper.dto.request.BucketPaymentUsageStatus;
import com.vuhlog.money_keeper.dto.request.DictionaryBucketPaymentRequest;
import com.vuhlog.money_keeper.dto.request.ExpenseRevenueHistoryRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryBucketPaymentResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRevenueHistory;
import org.springframework.data.domain.Page;

public interface DictionaryBucketPaymentService {
    DictionaryBucketPaymentResponse createDictionaryBucketPayment(
            DictionaryBucketPaymentRequest request, String userId);

    DictionaryBucketPaymentResponse updateDictionaryBucketPayment(String id, DictionaryBucketPaymentRequest request);

    DictionaryBucketPaymentResponse updateUsageStatus(String id, BucketPaymentUsageStatus status);

    void deleteDictionaryBucketPayment(String id);

    DictionaryBucketPaymentResponse getDictionaryBucketPaymentById(String id);

    List<DictionaryBucketPaymentResponse> getAllDictionaryBucketPayment(String userId, String search);

    Page<DictionaryBucketPaymentResponse> getDictionaryBucketPaymentPagination( String userId,String field, Integer pageNumber, Integer pageSize, String sort, String search);

    List<ExpenseRevenueHistory> getAllExpenseRevenueRegularsByIdAndDate(
            ExpenseRevenueHistoryRequest req, Integer pageNumber, Integer pageSize, String sort);

    Long getTotalExpenseByBucketPaymentId(ExpenseRevenueHistoryRequest req);

    Long getTotalRevenueByBucketPaymentId(ExpenseRevenueHistoryRequest req);

    Long getBalanceByBucketPaymentId(String bucketPaymentId);

    Long getMyTotalBalance();

    Long getTotalBalanceBySearch(String search);
}
