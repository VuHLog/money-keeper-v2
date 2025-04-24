package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private String title;
    private String content;
    private String type;
    private int readStatus;
    private String iconUrl;
    private String createdAt;
    private String href;

}
