package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.FinancialGoalRequest;
import com.vuhlog.money_keeper.dto.response.FinancialGoalResponse;
import com.vuhlog.money_keeper.entity.FinancialGoal;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "stringToLocalDate")
    FinancialGoal toFinancialGoal(FinancialGoalRequest request);

    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "stringToLocalDate")
    void updateFinancialGoal(@MappingTarget FinancialGoal financialGoal, FinancialGoalRequest request);

    @Mapping(target = "deadline", source = "deadline", qualifiedByName = "localDateToString")
    @Mapping(target = "createdAt", source = "createdAt", qualifiedByName = "timestampToString")
    @Mapping(target = "updatedAt", source = "updatedAt", qualifiedByName = "timestampToString")
    FinancialGoalResponse toFinancialGoalResponse(FinancialGoal financialGoal);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String dateString) {
        if(dateString == null || dateString.isEmpty()) return null;
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
    }

    @Named("localDateToString")
    default String localDateToString(LocalDate date) {
        if(date == null) return "";
        return date.toString();
    }

    @Named("timestampToString")
    default String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }
}
