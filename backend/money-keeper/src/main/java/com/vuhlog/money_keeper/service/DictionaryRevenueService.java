package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.DictionaryRevenueRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryRevenueResponse;

import java.util.List;

public interface DictionaryRevenueService {
    DictionaryRevenueResponse createDictionaryRevenue(DictionaryRevenueRequest request);

    DictionaryRevenueResponse updateDictionaryRevenue(String id, DictionaryRevenueRequest request);

    void deleteDictionaryRevenueItem(String id);

    DictionaryRevenueResponse getDictionaryRevenueById(String id);

    List<DictionaryRevenueResponse> getAllDictionaryRevenue();

    List<DictionaryRevenueResponse> getAllDictionaryRevenueWithoutTransfer();
}
