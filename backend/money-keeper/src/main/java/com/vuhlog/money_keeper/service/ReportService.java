package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.dashboard.TotalExpenseRevenue;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReportService {
    List<ReportTransactionTypeReponse> getReportForTransactionType(ReportFilterOptionsRequest request);

    List<ReportExpenseCategory> getReportExpenseCategory(ReportFilterOptionsRequest request);

    List<ReportRevenueCategory> getReportRevenueCategory(ReportFilterOptionsRequest request);

    List<ReportDailyTrend> getReportDailyTrend(ReportFilterOptionsRequest request);
    List<ReportWeeklyTrend> getReportWeeklyTrend(ReportFilterOptionsRequest request);
    List<ReportMonthlyTrend> getReportMonthlyTrend(ReportFilterOptionsRequest request);
    List<ReportYearlyTrend> getReportYearlyTrend(ReportFilterOptionsRequest request);

    List<ReportBucketPaymentBalance> getReportBucketPaymentBalance();

    List<ReportBucketPaymentTypeBalance> getReportBucketPaymentTypeBalance();

    TotalExpenseRevenue getTotalExpenseRevenueThisMonth();

    Page<TransactionHistory> getAllTransactionHistory(String field, Integer pageNumber, Integer pageSize, String sort, ReportFilterOptionsRequest request);

    List<TransactionHistory> getAllTransactionHistoryNoPaging(ReportFilterOptionsRequest request);
}
