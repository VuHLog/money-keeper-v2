package com.vuhlog.money_keeper.mapper;

import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.entity.Notification;
import com.vuhlog.money_keeper.util.TimestampUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.sql.Timestamp;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toNotification(NotificationRequest request);

    @Named("timestampToStringIfTodayTime")
    @Mapping(target = "createdAt", expression = "java(timestampToStringIfTodayTime(notification.getCreatedAt()))")
    NotificationResponse toNotificationResponse(Notification notification);

    default String timestampToStringIfTodayTime(Timestamp timestamp) {
        return TimestampUtil.timestampToStringIfTodayTime(timestamp);
    }
}
