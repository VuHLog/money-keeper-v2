package com.vuhlog.money_keeper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DictionaryRevenueResponse {
    private String id;

    private String parentId;

    private String name;

    private boolean regular;

    private boolean systemDefault;

    private String iconUrl;
}
