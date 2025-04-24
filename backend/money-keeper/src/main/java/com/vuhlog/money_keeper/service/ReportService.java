package com.vuhlog.money_keeper.service;

import com.vuhlog.money_keeper.dto.request.ExpenseRevenueSituation;
import com.vuhlog.money_keeper.dto.request.ReportCategoryResponse;
import com.vuhlog.money_keeper.dto.request.TotalExpenseRevenueForCategoryRequest;
import com.vuhlog.money_keeper.dto.request.TotalExpenseRevenueRequest;
import com.vuhlog.money_keeper.dto.response.TotalExpenseRevenueResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.*;

import java.util.List;

public interface ReportService {
    TotalExpenseRevenueResponse getTotalExpenseRevenueByTimeOption(TotalExpenseRevenueRequest req);

    List<TotalExpenseRevenueForExpenseRevenueSituation> getTotalExpenseRevenueForExpenseRevenueSituation(ExpenseRevenueSituation req);

    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategory(TotalExpenseRevenueForCategoryRequest req);

    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByPresent(TotalExpenseRevenueForCategoryRequest req);

    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByOptional(TotalExpenseRevenueForCategoryRequest req);

    List<ReportCategoryResponse> getTotalExpenseByTimeOptionAndCategory(TotalExpenseRevenueRequest req);

    List<ReportCategoryResponse> getTotalRevenueByTimeOptionAndCategory(TotalExpenseRevenueRequest req);

    TotalExpenseByExpenseLimit getTotalExpenseByExpenseLimit(String expenseLimitId, String startDate, String endDate);

    TotalExpenseRevenueByPresent getReportExpenseRevenueByPresent(ExpenseRevenueSituation req);

    TotalExpenseRevenueByOptional getReportExpenseRevenueByOptional(ExpenseRevenueSituation req);
}
