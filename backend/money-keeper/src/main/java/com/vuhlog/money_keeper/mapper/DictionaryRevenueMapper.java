package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.DictionaryRevenueRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryRevenueResponse;
import com.vuhlog.money_keeper.entity.DictionaryRevenue;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryRevenueMapper {
    DictionaryRevenue toDictionaryRevenue(DictionaryRevenueRequest request);

    void updateDictionaryRevenueFromRequest(DictionaryRevenueRequest request, @MappingTarget DictionaryRevenue dictionaryRevenue);

    DictionaryRevenueResponse toDictionaryRevenueResponse(DictionaryRevenue dictionaryRevenue);
}
