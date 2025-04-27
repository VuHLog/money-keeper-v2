package com.vuhlog.money_keeper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseLimitSearchRequest {
    private String field;
    private Integer pageNumber;
    private Integer pageSize;
    private String sort;
    private String search;
    private String categoriesId;
    private String bucketPaymentIds;
    private Integer day;
    private Integer month;
    private Integer year;
}
