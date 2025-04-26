package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.RevenueRegularRequest;
import com.vuhlog.money_keeper.dto.response.RevenueRegularResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RevenueRegularService {
    List<RevenueRegularResponse> getAllMyRevenueRegular(String dictionaryBucketPaymentId);
    Page<RevenueRegularResponse> getAllMyRevenueRegularPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search);
    RevenueRegularResponse createRevenueRegular(RevenueRegularRequest request);
    void deleteRevenueRegular(String id);
    RevenueRegularResponse updateRevenueRegular(String id, RevenueRegularRequest request);
    RevenueRegularResponse getRevenueRegularById(String id);
}
