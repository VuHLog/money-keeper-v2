package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportRevenueCategory {
    Long getTotalRevenue();
    String getCategoryId();
    String getCategoryName();
    String getIconUrl();
    String getTime();
}
