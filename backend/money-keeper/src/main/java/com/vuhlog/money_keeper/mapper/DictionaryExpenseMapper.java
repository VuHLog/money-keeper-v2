package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.DictionaryExpenseRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryExpenseResponse;
import com.vuhlog.money_keeper.entity.DictionaryExpense;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryExpenseMapper {
    DictionaryExpense toDictionaryExpense(DictionaryExpenseRequest request);

    void updateDictionaryExpenseFromRequest(DictionaryExpenseRequest request, @MappingTarget DictionaryExpense dictionaryExpense);

    DictionaryExpenseResponse toDictionaryExpenseResponse(DictionaryExpense dictionaryExpense);
}
