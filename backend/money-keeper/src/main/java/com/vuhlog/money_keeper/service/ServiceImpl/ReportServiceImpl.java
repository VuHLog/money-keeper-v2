package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.TimeOptionExpenseRevenueSituationType;
import com.vuhlog.money_keeper.constants.TimeOptionType;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.ReportExpenseRevenueRepository;
import com.vuhlog.money_keeper.dao.RevenueRegularRepository;
import com.vuhlog.money_keeper.dto.request.*;
import com.vuhlog.money_keeper.dto.response.*;
import com.vuhlog.money_keeper.dto.response.responseinterface.*;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.mapper.ReportExpenseRevenueMapper;
import com.vuhlog.money_keeper.model.PeriodOfTime;
import com.vuhlog.money_keeper.service.ReportService;
import com.vuhlog.money_keeper.util.TimestampUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    public TotalExpenseRevenueResponse getTotalExpenseRevenueByTimeOption(TotalExpenseRevenueRequest req) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        List<String> bucketPaymentIds = req.getBucketPaymentIds();
        String bucketPaymentIdsJoin = bucketPaymentIds != null ? String.join(",", bucketPaymentIds) : null;
        List<String> categoriesId = req.getCategoriesId();
        String categoriesIdJoin = categoriesId != null ? String.join(",", categoriesId) : null;
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);
        TotalExpenseRevenueResponse totalExpenseRevenueResponse = new TotalExpenseRevenueResponse();
        if(timeOption.equals(TimeOptionType.TODAY.getType()) || timeOption.equals(TimeOptionType.THIS_WEEK.getType())){
            Long totalExpense = expenseRegularRepository.getTotalExpenseByMonthAndThisYear(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, categoriesIdJoin, userId);
            totalExpenseRevenueResponse.setTotalExpense(totalExpense == null ? 0 : totalExpense);
            Long totalRevenue = revenueRegularRepository.getTotalRevenueByMonthAndThisYear(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, categoriesIdJoin, userId);
            totalExpenseRevenueResponse.setTotalRevenue(totalRevenue == null ? 0 : totalRevenue);
            return totalExpenseRevenueResponse;
        }else if (timeOption.equals(TimeOptionType.THIS_MONTH.getType()) || timeOption.equals(TimeOptionType.THIS_QUARTER.getType()) || timeOption.equals(TimeOptionType.THIS_YEAR.getType())){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(periodOfTime.getStartDate());
            int startMonth = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            calendar.setTime(periodOfTime.getEndDate());
            int endMonth = calendar.get(Calendar.MONTH) + 1;
            Object[] result = reportExpenseRevenueRepository.getTotalExpenseRevenueByMonthAndThisYear(startMonth, endMonth, year, userId, bucketPaymentIdsJoin, categoriesIdJoin);
            if(result != null && result.length > 0) {
                Object[] object = (Object[]) result[0];
                if(object != null && object.length >= 2 && object[0] != null && object[1] != null){
                    totalExpenseRevenueResponse = new TotalExpenseRevenueResponse(
                            ((Number) object[0]).longValue(),
                            ((Number) object[1]).longValue()
                    );
                    return totalExpenseRevenueResponse;
                }else {
                    totalExpenseRevenueResponse.setTotalExpense(0);
                    totalExpenseRevenueResponse.setTotalRevenue(0);
                }
            } else {
                totalExpenseRevenueResponse.setTotalExpense(0);
                totalExpenseRevenueResponse.setTotalRevenue(0);
            }
        }
        return totalExpenseRevenueResponse;
    }

    @Override
    public List<TotalExpenseRevenueForExpenseRevenueSituation> getTotalExpenseRevenueForExpenseRevenueSituation(ExpenseRevenueSituation req) {
        String timeOption = req.getTimeOption();
        List<TotalExpenseRevenueForExpenseRevenueSituation> totalExpenseRevenue = new ArrayList<>();
        if(timeOption.equals(TimeOptionExpenseRevenueSituationType.MONTH.getType())){
            totalExpenseRevenue = reportExpenseRevenueRepository.getReportExpenseRevenueByYearOrderByMonth(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    req.getYear()
            );
        }else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.QUARTER.getType())){
            totalExpenseRevenue = reportExpenseRevenueRepository.getReportExpenseRevenueByYearOrderByQuarter(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    req.getYear()
            );
        }else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.YEAR.getType())){
            totalExpenseRevenue = reportExpenseRevenueRepository.getReportExpenseRevenueByYearOrderByYear(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    req.getStartYear(),
                    req.getEndYear()
            );
        }

        return totalExpenseRevenue;
    }

    @Override
    public TotalExpenseRevenueByPresent getReportExpenseRevenueByPresent(ExpenseRevenueSituation req) {
        return reportExpenseRevenueRepository.getReportExpenseRevenueByPresent(
                userCommon.getMyUserInfo().getId(),
                req.getBucketPaymentIds()
        );
    }

    @Override
    public TotalExpenseRevenueByOptional getReportExpenseRevenueByOptional(ExpenseRevenueSituation req) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate startDate = LocalDate.parse(req.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(req.getEndDate(), formatter);

        // start date -> end day of month
        LocalDate endOfStartMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // months in between
        YearMonth current = YearMonth.from(startDate).plusMonths(1);
        YearMonth endMonth = YearMonth.from(endDate);
        LocalDate startDateBetween = null;
        LocalDate endDateBetween = null;

        // start day of month end date -> end date
        LocalDate startDateOfMonthEndDate = null;

        if(endMonth.isAfter(current)){
            startDateBetween = current.atDay(1);
            endDateBetween = endMonth.minusMonths(1).atEndOfMonth();

            startDateOfMonthEndDate = endDate.withDayOfMonth(1);
        }else {
            endOfStartMonth = endDate;
        }
        return reportExpenseRevenueRepository.getReportExpenseRevenueByOptional(
                userCommon.getMyUserInfo().getId(),
                req.getBucketPaymentIds(),
                startDate,
                endOfStartMonth,
                startDateBetween,
                endDateBetween,
                startDateOfMonthEndDate,
                endDate
        );
    }

    @Override
    public List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategory(TotalExpenseRevenueForCategoryRequest req) {
        String timeOption = req.getTimeOption();
        List<TotalExpenseRevenueForCategory> totalExpenseRevenueForCategoryList= new ArrayList<>();
        if(timeOption.equals(TimeOptionExpenseRevenueSituationType.MONTH.getType())){
            totalExpenseRevenueForCategoryList = reportExpenseRevenueRepository.getTotalExpenseRevenueForCategory(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    req.getMonth(),
                    null,
                    req.getYear()
            );
        }else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.QUARTER.getType())){
            totalExpenseRevenueForCategoryList = reportExpenseRevenueRepository.getTotalExpenseRevenueForCategory(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    null,
                    req.getQuarter(),
                    req.getYear()
            );
        }else if(timeOption.equals(TimeOptionExpenseRevenueSituationType.YEAR.getType())){
            totalExpenseRevenueForCategoryList = reportExpenseRevenueRepository.getTotalExpenseRevenueForCategory(
                    userCommon.getMyUserInfo().getId(),
                    req.getBucketPaymentIds(),
                    null,
                    null,
                    req.getYear()
            );
        }

        return totalExpenseRevenueForCategoryList;
    }

    @Override
    public List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByPresent(TotalExpenseRevenueForCategoryRequest req) {
        String option = req.getPresentOption();
        String bucketPaymentIdsJoin = req.getBucketPaymentIds();
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(option, null, null);
        List<TotalExpenseRevenueForCategory> totalExpenseRevenueForCategoryList = new ArrayList<>();
        if(option.equals(TimeOptionType.TODAY.getType()) || option.equals(TimeOptionType.THIS_WEEK.getType())){
            return reportExpenseRevenueRepository.getTotalExpenseRevenueForCategoryByTodayOrThisWeek(userId, bucketPaymentIdsJoin, periodOfTime.getStartDate(), periodOfTime.getEndDate());
        }else if (option.equals(TimeOptionType.THIS_MONTH.getType()) || option.equals(TimeOptionType.THIS_QUARTER.getType()) || option.equals(TimeOptionType.THIS_YEAR.getType())){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(periodOfTime.getStartDate());
            int startMonth = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            calendar.setTime(periodOfTime.getEndDate());
            int endMonth = calendar.get(Calendar.MONTH) + 1;
            if(option.equals(TimeOptionType.THIS_YEAR.getType())){
                totalExpenseRevenueForCategoryList = reportExpenseRevenueRepository.getTotalExpenseRevenueForCategoryByThisMonthOrThisQuarterOrThisYear(userId, bucketPaymentIdsJoin, null, null, year);
            }else{
                totalExpenseRevenueForCategoryList = reportExpenseRevenueRepository.getTotalExpenseRevenueForCategoryByThisMonthOrThisQuarterOrThisYear(userId, bucketPaymentIdsJoin, startMonth, endMonth, year);

            }
        }
        return totalExpenseRevenueForCategoryList;
    }

    @Override
    public List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByOptional(TotalExpenseRevenueForCategoryRequest req) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate startDate = LocalDate.parse(req.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(req.getEndDate(), formatter);

        // start date -> end day of month
        LocalDate endOfStartMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // months in between
        YearMonth current = YearMonth.from(startDate).plusMonths(1);
        YearMonth endMonth = YearMonth.from(endDate);
        LocalDate startDateBetween = null;
        LocalDate endDateBetween = null;

        // start day of month end date -> end date
        LocalDate startDateOfMonthEndDate = null;

        if(endMonth.isAfter(current)){
            startDateBetween = current.atDay(1);
            endDateBetween = endMonth.minusMonths(1).atEndOfMonth();

            startDateOfMonthEndDate = endDate.withDayOfMonth(1);
        }else {
            endOfStartMonth = endDate;
        }
        return reportExpenseRevenueRepository.getTotalExpenseRevenueForCategoryByOptional(
                userCommon.getMyUserInfo().getId(),
                req.getBucketPaymentIds(),
                startDate,
                endOfStartMonth,
                startDateBetween,
                endDateBetween,
                startDateOfMonthEndDate,
                endDate
        );
    }

    @Override
    public Long getTotalExpenseByPeriodOfTime(PeriodOfTimeRequest req) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate startDate = LocalDate.parse(req.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(req.getEndDate(), formatter);

        // start date -> end day of month
        LocalDate endOfStartMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // months in between
        YearMonth current = YearMonth.from(startDate).plusMonths(1);
        YearMonth endMonth = YearMonth.from(endDate);
        LocalDate startDateBetween = null;
        LocalDate endDateBetween = null;

        // start day of month end date -> end date
        LocalDate startDateOfMonthEndDate = null;

        if(endMonth.isAfter(current)){
            startDateBetween = current.atDay(1);
            endDateBetween = endMonth.minusMonths(1).atEndOfMonth();

            startDateOfMonthEndDate = endDate.withDayOfMonth(1);
        }else {
            endOfStartMonth = endDate;
        }
        return reportExpenseRevenueRepository.getTotalExpenseByPeriodOfTime(
                userCommon.getMyUserInfo().getId(),
                req.getBucketPaymentIds(),
                req.getCategoriesId(),
                startDate,
                endOfStartMonth,
                startDateBetween,
                endDateBetween,
                startDateOfMonthEndDate,
                endDate
        );
    }

    @Override
    public List<ReportCategoryResponse> getTotalExpenseByTimeOptionAndCategory(TotalExpenseRevenueRequest req) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        List<String> bucketPaymentIds = req.getBucketPaymentIds();
        if (req.getIsSelectAllBucketPayment() != null && req.getIsSelectAllBucketPayment()) {
            bucketPaymentIds = null;
        }
        String bucketPaymentIdsJoin = bucketPaymentIds != null ? String.join(",", bucketPaymentIds) : null;
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);
//        if(timeOption.equals(TimeOptionType.TODAY.getType()) || timeOption.equals(TimeOptionType.THIS_WEEK.getType())){
//            List<Object[]> results = expenseRegularRepository.getTotalExpenseByTimeAndCategory(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, userId);
//            return convertToReportCategoryResponse(results);
//        }else if (timeOption.equals(TimeOptionType.THIS_MONTH.getType()) || timeOption.equals(TimeOptionType.THIS_QUARTER.getType()) || timeOption.equals(TimeOptionType.THIS_YEAR.getType())){
//            List<Object[]> results = expenseRegularRepository.getTotalExpenseByTimeAndCategory(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, userId);
//            return convertToReportCategoryResponse(results);
//        }
        List<Object[]> results = expenseRegularRepository.getTotalExpenseByTimeAndCategory(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, userId);
        return convertToReportCategoryResponse(results);
    }

    @Override
    public List<ReportCategoryResponse> getTotalRevenueByTimeOptionAndCategory(TotalExpenseRevenueRequest req) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        List<String> bucketPaymentIds = req.getBucketPaymentIds();
        if (req.getIsSelectAllBucketPayment() != null && req.getIsSelectAllBucketPayment()) {
            bucketPaymentIds = null;
        }
        String bucketPaymentIdsJoin = bucketPaymentIds != null ? String.join(",", bucketPaymentIds) : null;
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);
        List<Object[]> results = revenueRegularRepository.getTotalRevenueByTimeAndCategory(periodOfTime.getStartDate(), periodOfTime.getEndDate(), bucketPaymentIdsJoin, userId);
        return convertToReportCategoryResponse(results);
    }

    @Override
    public TotalExpenseByExpenseLimit getTotalExpenseByExpenseLimit(String expenseLimitId, String startDate, String endDate) {
        Timestamp startTime = Timestamp.valueOf(startDate);
        Timestamp endTime = null;
        if(endDate != null && !endDate.isEmpty()) {
            endTime = Timestamp.valueOf(endDate);
        }
        return expenseRegularRepository.getTotalExpenseByUserIdGroupByExpenseLimit(userCommon.getMyUserInfo().getId(), expenseLimitId, startTime, endTime);
    }

    private List<ReportCategoryResponse> convertToReportCategoryResponse(List<Object[]> list) {
        return list.stream().map(obj ->
                    new ReportCategoryResponse(
                            ((Number) obj[0]).longValue(), // total
                            (String) obj[1], //type
                            (String) obj[2], // category id
                            (String) obj[3], // name
                            (String) obj[4] // icon url
                    )
        ).collect(Collectors.toList());
    }
}
