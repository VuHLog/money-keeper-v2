package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.ExpenseLimitRequest;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import com.vuhlog.money_keeper.entity.ExpenseLimit;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface ExpenseLimitMapper {
    @Named("stringToTimestamp")
    @Mapping(target = "startDate", expression = "java(stringToTimestamp(req.getStartDate()))")
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "startDateLimit", expression = "java(stringToTimestamp(req.getStartDateLimit()))")
    @Mapping(target = "endDateLimit", expression = "java(stringToTimestamp(req.getEndDateLimit()))")
    ExpenseLimit toExpenseLimit(ExpenseLimitRequest req);

    @Named("stringToTimestamp")
    @Mapping(target = "startDate", expression = "java(stringToTimestamp(req.getStartDate()))")
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "startDateLimit", expression = "java(stringToTimestamp(req.getStartDateLimit()))")
    @Mapping(target = "endDateLimit", expression = "java(stringToTimestamp(req.getEndDateLimit()))")
    void updateExpenseLimit(@MappingTarget ExpenseLimit expenseLimit, ExpenseLimitRequest req);

    @Named("timestampToStringOnlyDate")
    @Mapping(target = "startDate", expression = "java(timestampToStringOnlyDate(expenseLimit.getStartDate()))")
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "startDateLimit", expression = "java(timestampToStringOnlyDate(expenseLimit.getStartDateLimit()))")
    @Mapping(target = "endDateLimit", expression = "java(timestampToStringOnlyDate(expenseLimit.getEndDateLimit()))")
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "bucketPayments", ignore = true)
    ExpenseLimitResponse toExpenseLimitResponse(ExpenseLimit expenseLimit);

    default Timestamp stringToTimestamp(String dateString) {
        return !dateString.isEmpty()? TimestampUtil.stringToTimestamp(dateString) : null;
    }

    default String timestampToString(Timestamp timestamp) {
        return timestamp != null ? TimestampUtil.timestampToString(timestamp) : null;
    }

    default String timestampToStringOnlyDate(Timestamp timestamp) {
        return timestamp != null ? TimestampUtil.timestampToStringOnlyDate(timestamp) : null;
    }
}
