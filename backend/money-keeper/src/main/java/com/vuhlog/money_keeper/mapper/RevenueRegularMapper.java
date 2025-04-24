package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.RevenueRegularRequest;
import com.vuhlog.money_keeper.dto.request.TransferRequest;
import com.vuhlog.money_keeper.dto.response.RevenueRegularResponse;
import com.vuhlog.money_keeper.entity.ExpenseRegular;
import com.vuhlog.money_keeper.entity.RevenueRegular;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface RevenueRegularMapper {
    @Named("stringToTimestamp")
    @Mapping(target = "revenueDate", expression = "java(stringToTimestamp(request.getRevenueDate()))")
    @Mapping(target = "dictionaryBucketPayment", ignore = true)
    @Mapping(target = "dictionaryRevenue", ignore = true)
    @Mapping(target = "tripEvent", ignore = true)
    @Mapping(target = "collectMoneyWho", ignore = true)
    RevenueRegular toRevenueRegular(RevenueRegularRequest request);

    @Named("timestampToString")
    @Mapping(target = "revenueDate", expression = "java(timestampToString(revenueRegular.getRevenueDate()))")
    RevenueRegularResponse toRevenueRegularResponse(RevenueRegular revenueRegular);

    @Named("stringToTimestamp")
    @Mapping(target = "revenueDate", expression = "java(stringToTimestamp(request.getExpenseDate()))")
    @Mapping(target = "dictionaryBucketPayment", ignore = true)
    @Mapping(target = "senderAccount", ignore = true)
    RevenueRegular toRevenueRegularFromTransferRequest(TransferRequest request);

    @Named("stringToTimestamp")
    @Mapping(target = "revenueDate", expression = "java(stringToTimestamp(request.getExpenseDate()))")
    @Mapping(target = "dictionaryBucketPayment", ignore = true)
    @Mapping(target = "senderAccount", ignore = true)
    void updateRevenueRegularFromTransferRequest(TransferRequest request, @MappingTarget RevenueRegular revenueRegular);

    @Named("stringToTimestamp")
    @Mapping(target = "revenueDate", expression = "java(stringToTimestamp(request.getRevenueDate()))")
    @Mapping(target = "dictionaryBucketPayment", ignore = true)
    @Mapping(target = "dictionaryRevenue", ignore = true)
    @Mapping(target = "tripEvent", ignore = true)
    @Mapping(target = "collectMoneyWho", ignore = true)
    void updateRevenueRegularFromRequest(RevenueRegularRequest request, @MappingTarget RevenueRegular revenueRegular);

    default Timestamp stringToTimestamp(String dateString) {
        return TimestampUtil.stringToTimestamp(dateString);
    }

    default String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }
}
