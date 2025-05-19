package com.vuhlog.money_keeper.dto.response.responseinterface.report;

public interface ReportCategory {
    String getId();

    String getName();

    String getType();

    Long getTotalTransaction();

    Long getTotalAmount();
}
