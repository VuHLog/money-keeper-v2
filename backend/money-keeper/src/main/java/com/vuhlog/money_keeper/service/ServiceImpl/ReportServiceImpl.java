package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.ReportExpenseRevenueRepository;
import com.vuhlog.money_keeper.dao.RevenueRegularRepository;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportExpenseCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportRevenueCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;
import com.vuhlog.money_keeper.mapper.ReportExpenseRevenueMapper;
import com.vuhlog.money_keeper.service.ReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final UserCommon userCommon;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final RevenueRegularRepository revenueRegularRepository;
    private final ReportExpenseRevenueRepository reportExpenseRevenueRepository;
    private final ReportExpenseRevenueMapper reportExpenseRevenueMapper;

    @PersistenceContext
    private EntityManager em;


    @Override
    public List<ReportTransactionTypeReponse> getReportForTransactionType(ReportFilterOptionsRequest request) {
        String timeOption = request.getTimeOption();
        String userId = userCommon.getMyUserInfo().getId();
        String bucketPaymentIds = request.getBucketPaymentIds();
        List<ReportTransactionTypeReponse> response = new ArrayList<>();
        String start = request.getCustomTimeRange().get(0);
        String end = request.getCustomTimeRange().get(1);
        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            response = reportExpenseRevenueRepository.getReportForTransactionTypeByMonth(userId, bucketPaymentIds, startMonth.atDay(1), endMonth.atEndOfMonth());
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            response = reportExpenseRevenueRepository.getReportForTransactionTypeByYear(userId, bucketPaymentIds, startYear, endYear);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            response = reportExpenseRevenueRepository.getReportForTransactionTypeByOptional(userId, bucketPaymentIds, startDate, endDate);
        }
        return response;
    }

    @Override
    public List<ReportExpenseCategory> getReportExpenseCategory(ReportFilterOptionsRequest request) {
        String timeOption = request.getTimeOption();
        String userId = userCommon.getMyUserInfo().getId();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String categoriesId = request.getExpenseCategoriesId();
        List<ReportExpenseCategory> response = new ArrayList<>();
        String start = request.getCustomTimeRange().get(0);
        String end = request.getCustomTimeRange().get(1);
        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            response = reportExpenseRevenueRepository.getReportExpenseCategoryByMonth(userId, bucketPaymentIds, categoriesId, startMonth.atDay(1), endMonth.atEndOfMonth());
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            response = reportExpenseRevenueRepository.getReportExpenseCategoryByYear(userId, bucketPaymentIds, categoriesId, startYear, endYear);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            response = reportExpenseRevenueRepository.getReportExpenseCategoryByOptional(userId, bucketPaymentIds, categoriesId, startDate, endDate);
        }
        return response;
    }

    @Override
    public List<ReportRevenueCategory> getReportRevenueCategory(ReportFilterOptionsRequest request) {
        String timeOption = request.getTimeOption();
        String userId = userCommon.getMyUserInfo().getId();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String categoriesId = request.getExpenseCategoriesId();
        List<ReportRevenueCategory> response = new ArrayList<>();
        String start = request.getCustomTimeRange().get(0);
        String end = request.getCustomTimeRange().get(1);
        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            response = reportExpenseRevenueRepository.getReportRevenueCategoryByMonth(userId, bucketPaymentIds, categoriesId, startMonth.atDay(1), endMonth.atEndOfMonth());
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            response = reportExpenseRevenueRepository.getReportRevenueCategoryByYear(userId, bucketPaymentIds, categoriesId, startYear, endYear);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            response = reportExpenseRevenueRepository.getReportRevenueCategoryByOptional(userId, bucketPaymentIds, categoriesId, startDate, endDate);
        }
        return response;
    }
}
