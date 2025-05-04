package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportExpenseCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportRevenueCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;

import java.util.List;

public interface ReportService {
    List<ReportTransactionTypeReponse> getReportForTransactionType(ReportFilterOptionsRequest request);

    List<ReportExpenseCategory> getReportExpenseCategory(ReportFilterOptionsRequest request);

    List<ReportRevenueCategory> getReportRevenueCategory(ReportFilterOptionsRequest request);
}
