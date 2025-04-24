package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.response.ReportExpenseRevenueResponse;
import com.vuhlog.money_keeper.entity.ReportExpenseRevenue;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface ReportExpenseRevenueMapper {
    @Named("timestampToString")
    @Mapping(target = "createdAt", expression = "java(timestampToString(reportExpenseRevenue.getCreatedAt()))")
    @Mapping(target = "updatedAt", expression = "java(timestampToString(reportExpenseRevenue.getUpdatedAt()))")
    @Mapping(target = "userId", expression = "java(reportExpenseRevenue.getUser().getId())")
    ReportExpenseRevenueResponse toReportExpenseRevenueResponse(ReportExpenseRevenue reportExpenseRevenue);

    default Timestamp stringToTimestamp(String dateString) {
        return TimestampUtil.stringToTimestamp(dateString);
    }

    default String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }
}
