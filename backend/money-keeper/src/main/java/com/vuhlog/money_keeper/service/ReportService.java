package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.ReportBucketPaymentBalanceDTO;
import com.vuhlog.money_keeper.dto.response.ReportBucketPaymentTypeBalanceDTO;
import com.vuhlog.money_keeper.dto.response.ReportTotalBucketPaymentDTO;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TotalExpenseRevenue;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReportService {
    Long getTotalExpense(ReportFilterOptionsRequest request);

    Long getTotalRevenue(ReportFilterOptionsRequest request);

    List<ReportTransactionTypeReponse> getReportForTransactionType(ReportFilterOptionsRequest request);

    List<ReportExpenseCategory> getReportExpenseCategory(ReportFilterOptionsRequest request);

    List<ReportRevenueCategory> getReportRevenueCategory(ReportFilterOptionsRequest request);

    List<ReportDailyTrend> getReportDailyTrend(ReportFilterOptionsRequest request);
    List<ReportWeeklyTrend> getReportWeeklyTrend(ReportFilterOptionsRequest request);
    List<ReportMonthlyTrend> getReportMonthlyTrend(ReportFilterOptionsRequest request);
    List<ReportYearlyTrend> getReportYearlyTrend(ReportFilterOptionsRequest request);

    List<ReportBucketPaymentBalanceDTO> getReportBucketPaymentBalance();

    List<ReportBucketPaymentTypeBalanceDTO> getReportBucketPaymentTypeBalance();

    List<AccountBalanceFluctuation> getAccountBalanceFluctuation(ReportFilterOptionsRequest request);

    TotalExpenseRevenue getTotalExpenseRevenueThisMonth();

    Page<TransactionHistory> getAllTransactionHistory(String field, Integer pageNumber, Integer pageSize, String sort, ReportFilterOptionsRequest request);

    List<TransactionHistory> getAllTransactionHistoryNoPaging(ReportFilterOptionsRequest request);

    Page<ReportBucketPayment> getBucketPaymentReport(String field, Integer pageNumber, Integer pageSize, String sort,ReportFilterOptionsRequest request);

    List<ReportBucketPayment> getBucketPaymentReportNoPaging(ReportFilterOptionsRequest request);

    ReportTotalBucketPaymentDTO getReportTotalBucketPayment(ReportFilterOptionsRequest request);

    Page<ReportCategory> getReportCategory(String field, Integer pageNumber, Integer pageSize, String sort, ReportFilterOptionsRequest request);

    List<ReportCategory> getReportCategoryNoPaging(ReportFilterOptionsRequest request);
}
