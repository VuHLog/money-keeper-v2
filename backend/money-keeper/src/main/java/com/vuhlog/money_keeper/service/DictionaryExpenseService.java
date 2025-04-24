package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.DictionaryExpenseRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryExpenseResponse;

import java.util.List;

public interface DictionaryExpenseService {
    DictionaryExpenseResponse createDictionaryExpense(DictionaryExpenseRequest request);

    DictionaryExpenseResponse updateDictionaryExpense(String id, DictionaryExpenseRequest request);

    void deleteDictionaryExpenseItem(String id);

    DictionaryExpenseResponse getDictionaryExpenseById(String id);

    List<DictionaryExpenseResponse> getAllDictionaryExpense();

    List<DictionaryExpenseResponse> getAllDictionaryExpenseWithoutTransfer(String search);
}
