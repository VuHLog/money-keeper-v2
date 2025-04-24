package com.vuhlog.money_keeper.dto.request;

import com.vuhlog.money_keeper.dto.response.DictionaryExpenseResponse;
import com.vuhlog.money_keeper.dto.response.DictionaryRevenueResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportCategoryResponse {
    private long total;
    private String type;
    private String categoryId;
    private String name;
    private String iconUrl;
}
