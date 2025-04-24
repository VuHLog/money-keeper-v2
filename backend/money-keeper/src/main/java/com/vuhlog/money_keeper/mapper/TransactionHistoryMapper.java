package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.response.TransactionHistoryResponse;
import com.vuhlog.money_keeper.entity.TransactionHistory;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface TransactionHistoryMapper {
    @Named("timestampToString")
    @Mapping(target = "updatedAt", expression = "java(timestampToString(transactionHistory.getUpdatedAt()))")
    TransactionHistoryResponse toTransactionHistoryResponse(TransactionHistory transactionHistory);

    default Timestamp stringToTimestamp(String dateString) {
        return TimestampUtil.stringToTimestamp(dateString);
    }

    default String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }
}
