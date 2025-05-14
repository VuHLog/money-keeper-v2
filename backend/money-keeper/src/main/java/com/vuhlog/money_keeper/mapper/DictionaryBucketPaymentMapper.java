package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.DictionaryBucketPaymentRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryBucketPaymentResponse;
import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DictionaryBucketPaymentMapper {

    @Mapping(target = "bank", ignore = true)
    DictionaryBucketPayment toDictionaryBucketPayment(DictionaryBucketPaymentRequest request);

    @Mapping(target = "bank", ignore = true)
    @Mapping(target = "balance", ignore = true)
    void updateDictionaryBucketPaymentFromRequest(DictionaryBucketPaymentRequest request,@MappingTarget DictionaryBucketPayment dictionaryBucketPayment);

    DictionaryBucketPaymentResponse toDictionaryBucketResponse(DictionaryBucketPayment dictionaryBucketPayment);
}
