package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.ReportTimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.dao.DictionaryBucketPaymentRepository;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.ReportExpenseRevenueRepository;
import com.vuhlog.money_keeper.dao.RevenueRegularRepository;
import com.vuhlog.money_keeper.dao.specification.DictionaryBucketPaymentSpecification;
import com.vuhlog.money_keeper.dto.request.ReportFilterOptionsRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.TotalExpenseRevenue;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import com.vuhlog.money_keeper.mapper.ReportExpenseRevenueMapper;
import com.vuhlog.money_keeper.service.ReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final UserCommon userCommon;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final RevenueRegularRepository revenueRegularRepository;
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final ReportExpenseRevenueRepository reportExpenseRevenueRepository;
    private final ReportExpenseRevenueMapper reportExpenseRevenueMapper;

    @PersistenceContext
    private EntityManager em;


    @Override
    public Long getTotalExpense(ReportFilterOptionsRequest request) {
        String transactionType = request.getTransactionType();
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        return reportExpenseRevenueRepository.getTotalExpense(userId, bucketPaymentIds, request.getExpenseCategoriesId(), startDate, endDate);
    }

    @Override
    public Long getTotalRevenue(ReportFilterOptionsRequest request) {
        String transactionType = request.getTransactionType();
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        return reportExpenseRevenueRepository.getTotalRevenue(userId, bucketPaymentIds, request.getRevenueCategoriesId(), startDate, endDate);
    }

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

    @Override
    public List<ReportDailyTrend> getReportDailyTrend(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        List<ReportDailyTrend> response = new ArrayList<>();
        if(transactionType.equals(TransactionType.EXPENSE.getType())){
            response = reportExpenseRevenueRepository.getReportExpenseDailyTrend(userId, bucketPaymentIds);
        }else if(transactionType.equals(TransactionType.REVENUE.getType())){
            response = reportExpenseRevenueRepository.getReportRevenueDailyTrend(userId, bucketPaymentIds);
        }
        return response;
    }

    @Override
    public List<ReportWeeklyTrend> getReportWeeklyTrend(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        List<ReportWeeklyTrend> response = new ArrayList<>();
        if(transactionType.equals(TransactionType.EXPENSE.getType())){
            response = reportExpenseRevenueRepository.getReportExpenseWeeklyTrend(userId, bucketPaymentIds);
        }else if(transactionType.equals(TransactionType.REVENUE.getType())){
            response = reportExpenseRevenueRepository.getReportRevenueWeeklyTrend(userId, bucketPaymentIds);
        }
        return response;
    }

    @Override
    public List<ReportMonthlyTrend> getReportMonthlyTrend(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        List<ReportMonthlyTrend> response = new ArrayList<>();
        if(transactionType.equals(TransactionType.EXPENSE.getType())){
            response = reportExpenseRevenueRepository.getReportExpenseMonthlyTrend(userId, bucketPaymentIds);
        }else if(transactionType.equals(TransactionType.REVENUE.getType())){
            response = reportExpenseRevenueRepository.getReportRevenueMonthlyTrend(userId, bucketPaymentIds);
        }
        return response;
    }

    @Override
    public List<ReportYearlyTrend> getReportYearlyTrend(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        List<ReportYearlyTrend> response = new ArrayList<>();
        if(transactionType.equals(TransactionType.EXPENSE.getType())){
            response = reportExpenseRevenueRepository.getReportExpenseYearlyTrend(userId, bucketPaymentIds);
        }else if(transactionType.equals(TransactionType.REVENUE.getType())){
            response = reportExpenseRevenueRepository.getReportRevenueYearlyTrend(userId, bucketPaymentIds);
        }
        return response;
    }

    @Override
    public List<ReportBucketPaymentBalance> getReportBucketPaymentBalance() {
        String userId = userCommon.getMyUserInfo().getId();
        return reportExpenseRevenueRepository.getReportBucketPaymentBalance(userId);
    }

    @Override
    public List<ReportBucketPaymentTypeBalance> getReportBucketPaymentTypeBalance() {
        String userId = userCommon.getMyUserInfo().getId();
        return reportExpenseRevenueRepository.getReportBucketPaymentTypeBalance(userId);
    }

    @Override
    public List<AccountBalanceFluctuation> getAccountBalanceFluctuation(ReportFilterOptionsRequest request) {
        String timeOption = request.getTimeOption();
        String userId = userCommon.getMyUserInfo().getId();
        List<AccountBalanceFluctuation> response = new ArrayList<>();
        List<DictionaryBucketPayment> bucketPayments = new ArrayList<>();
        Specification<DictionaryBucketPayment> specs = Specification.where(null);
        specs = specs.and(DictionaryBucketPaymentSpecification.filterByUserId(userId));
        bucketPayments = dictionaryBucketPaymentRepository.findAll(specs);
        String start = request.getCustomTimeRange().get(0);
        String end = request.getCustomTimeRange().get(1);
        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            response = reportExpenseRevenueRepository.getAccountBalanceFluctuationByMonth(userId, startMonth.atDay(1), endMonth.atEndOfMonth());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
            String startMonthStr = startMonth.format(formatter);
            List<AccountBalanceFluctuation> initial = new ArrayList<>();
            initial = reportExpenseRevenueRepository.getNearestAccountBalanceFluctuationByMonth(userId, startMonth.atDay(1));
            
            // Kiểm tra và thêm dữ liệu cho tháng bắt đầu nếu chưa có
            for (DictionaryBucketPayment bucketPayment : bucketPayments) {
                boolean hasDataForStartMonth = false;
                
                // Kiểm tra xem đã có dữ liệu cho tháng bắt đầu và tài khoản này chưa
                for (AccountBalanceFluctuation item : response) {
                    if (item.getTime().equals(startMonthStr) && 
                        item.getBucketPaymentId().equals(bucketPayment.getId())) {
                        hasDataForStartMonth = true;
                        break;
                    }
                }
                
                // Nếu chưa có dữ liệu cho tháng bắt đầu, thêm dữ liệu từ initial hoặc initialBalance
                if (!hasDataForStartMonth) {
                    final String bucketPaymentId = bucketPayment.getId();
                    Long balanceValue = bucketPayment.getInitialBalance();
                    
                    // Tìm giá trị trong initial (nếu có)
                    for (AccountBalanceFluctuation initialItem : initial) {
                        if (initialItem.getBucketPaymentId().equals(bucketPaymentId)) {
                            balanceValue = initialItem.getBalanceAfterTransaction();
                            break;
                        }
                    }
                    
                    // Thêm dữ liệu vào response
                    Long finalBalanceValue = balanceValue;
                    response.add(0, new AccountBalanceFluctuation() {
                        @Override
                        public String getTime() {
                            return startMonthStr;
                        }
                        
                        @Override
                        public String getBucketPaymentId() {
                            return bucketPaymentId;
                        }
                        
                        @Override
                        public String getAccountName() {
                            return bucketPayment.getAccountName();
                        }
                        
                        @Override
                        public Long getBalanceAfterTransaction() {
                            return finalBalanceValue;
                        }
                        
                        @Override
                        public Long getCurrentBalance() {
                            return bucketPayment.getBalance();
                        }
                    });
                }
            }
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            response = reportExpenseRevenueRepository.getAccountBalanceFluctuationByYear(userId, startYear, endYear);
            String startYearStr = String.valueOf(startYear);
            List<AccountBalanceFluctuation> initial = new ArrayList<>();
            initial = reportExpenseRevenueRepository.getNearestAccountBalanceFluctuationByYear(userId, startYear);
            
            // Kiểm tra và thêm dữ liệu cho năm bắt đầu nếu chưa có
            for (DictionaryBucketPayment bucketPayment : bucketPayments) {
                boolean hasDataForStartYear = false;
                
                // Kiểm tra xem đã có dữ liệu cho năm bắt đầu và tài khoản này chưa
                for (AccountBalanceFluctuation item : response) {
                    if (item.getTime().equals(startYearStr) && 
                        item.getBucketPaymentId().equals(bucketPayment.getId())) {
                        hasDataForStartYear = true;
                        break;
                    }
                }
                
                // Nếu chưa có dữ liệu cho năm bắt đầu, thêm dữ liệu từ initial hoặc initialBalance
                if (!hasDataForStartYear) {
                    final String bucketPaymentId = bucketPayment.getId();
                    Long balanceValue = bucketPayment.getInitialBalance();
                    
                    // Tìm giá trị trong initial (nếu có)
                    for (AccountBalanceFluctuation initialItem : initial) {
                        if (initialItem.getBucketPaymentId().equals(bucketPaymentId)) {
                            balanceValue = initialItem.getBalanceAfterTransaction();
                            break;
                        }
                    }
                    
                    // Thêm dữ liệu vào response
                    Long finalBalanceValue = balanceValue;
                    response.add(0, new AccountBalanceFluctuation() {
                        @Override
                        public String getTime() {
                            return startYearStr;
                        }
                        
                        @Override
                        public String getBucketPaymentId() {
                            return bucketPaymentId;
                        }
                        
                        @Override
                        public String getAccountName() {
                            return bucketPayment.getAccountName();
                        }
                        
                        @Override
                        public Long getBalanceAfterTransaction() {
                            return finalBalanceValue;
                        }
                        
                        @Override
                        public Long getCurrentBalance() {
                            return bucketPayment.getBalance();
                        }
                    });
                }
            }
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            LocalDate startDate = LocalDate.parse(start);
            LocalDate endDate = LocalDate.parse(end);
            response = reportExpenseRevenueRepository.getAccountBalanceFluctuationByOptional(userId, startDate, endDate);
            String startDateStr = startDate.toString();
            List<AccountBalanceFluctuation> initial = new ArrayList<>();
            initial = reportExpenseRevenueRepository.getNearestAccountBalanceFluctuationByOptional(userId, startDate);
            
            // Kiểm tra và thêm dữ liệu cho ngày bắt đầu nếu chưa có
            for (DictionaryBucketPayment bucketPayment : bucketPayments) {
                boolean hasDataForStartDate = false;
                
                // Kiểm tra xem đã có dữ liệu cho ngày bắt đầu và tài khoản này chưa
                for (AccountBalanceFluctuation item : response) {
                    if (item.getTime().equals(startDateStr) && 
                        item.getBucketPaymentId().equals(bucketPayment.getId())) {
                        hasDataForStartDate = true;
                        break;
                    }
                }
                
                // Nếu chưa có dữ liệu cho ngày bắt đầu, thêm dữ liệu từ initial hoặc initialBalance
                if (!hasDataForStartDate) {
                    final String bucketPaymentId = bucketPayment.getId();
                    Long balanceValue = bucketPayment.getInitialBalance();
                    
                    // Tìm giá trị trong initial (nếu có)
                    for (AccountBalanceFluctuation initialItem : initial) {
                        if (initialItem.getBucketPaymentId().equals(bucketPaymentId)) {
                            balanceValue = initialItem.getBalanceAfterTransaction();
                            break;
                        }
                    }
                    
                    // Thêm dữ liệu vào response
                    Long finalBalanceValue = balanceValue;
                    response.add(0, new AccountBalanceFluctuation() {
                        @Override
                        public String getTime() {
                            return startDateStr;
                        }
                        
                        @Override
                        public String getBucketPaymentId() {
                            return bucketPaymentId;
                        }
                        
                        @Override
                        public String getAccountName() {
                            return bucketPayment.getAccountName();
                        }
                        
                        @Override
                        public Long getBalanceAfterTransaction() {
                            return finalBalanceValue;
                        }
                        
                        @Override
                        public Long getCurrentBalance() {
                            return bucketPayment.getBalance();
                        }
                    });
                }
            }
        }
        return response;
    }

    @Override
    public TotalExpenseRevenue getTotalExpenseRevenueThisMonth() {
        return reportExpenseRevenueRepository.getTotalExpenseRevenueThisMonthByUserId(userCommon.getMyUserInfo().getId());
    }

    @Override
    public Page<TransactionHistory> getAllTransactionHistory(String field, Integer pageNumber, Integer pageSize, String sort, ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;
        Sort sortable = Sort.by("date").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        if(transactionType.equals(TransactionType.EXPENSE.getType())) {
            return reportExpenseRevenueRepository.getAllExpenseHistory(userId, bucketPaymentIds, request.getExpenseCategoriesId() , startDate, endDate, pageable);
        } else if (transactionType.equals(TransactionType.REVENUE.getType())){
            return reportExpenseRevenueRepository.getAllRevenueHistory(userId, bucketPaymentIds, request.getRevenueCategoriesId(), startDate, endDate, pageable);
        } else {
            return reportExpenseRevenueRepository.getAllExpenseRevenueHistory(userId, bucketPaymentIds, startDate, endDate, pageable);
        }
    }

    @Override
    public List<TransactionHistory> getAllTransactionHistoryNoPaging(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String transactionType = request.getTransactionType();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        if(transactionType.equals(TransactionType.EXPENSE.getType())) {
            return reportExpenseRevenueRepository.getAllExpenseHistoryNoPaging(userId, bucketPaymentIds, request.getExpenseCategoriesId() , startDate, endDate);
        } else if (transactionType.equals(TransactionType.REVENUE.getType())){
            return reportExpenseRevenueRepository.getAllRevenueHistoryNoPaging(userId, bucketPaymentIds, request.getRevenueCategoriesId(), startDate, endDate);
        } else {
            return reportExpenseRevenueRepository.getAllExpenseRevenueHistoryNoPaging(userId, bucketPaymentIds, startDate, endDate);
        }
    }

    @Override
    public Page<ReportBucketPayment> getBucketPaymentReport(String field, Integer pageNumber, Integer pageSize, String sort,ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return reportExpenseRevenueRepository.getBucketPaymentReport(userId, bucketPaymentIds, startDate, endDate, pageable);
    }

    @Override
    public List<ReportBucketPayment> getBucketPaymentReportNoPaging(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }
        return reportExpenseRevenueRepository.getBucketPaymentReportNoPaging(userId, bucketPaymentIds, startDate, endDate);
    }

    @Override
    public ReportTotalBucketPayment getReportTotalBucketPayment(ReportFilterOptionsRequest request){
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String bucketPaymentIds = request.getBucketPaymentIds();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }
        return reportExpenseRevenueRepository.getTotalBucketPayment(userId, bucketPaymentIds, startDate, endDate);
    }

    @Override
    public Page<ReportCategory> getReportCategory(String field, Integer pageNumber, Integer pageSize, String sort,ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String expenseCategoriesId = request.getExpenseCategoriesId();
        String revenueCategoriesId = request.getRevenueCategoriesId();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return reportExpenseRevenueRepository.getCategoryReport(userId, expenseCategoriesId, revenueCategoriesId, startDate, endDate, pageable);
    }

    @Override
    public List<ReportCategory> getReportCategoryNoPaging(ReportFilterOptionsRequest request) {
        String userId = userCommon.getMyUserInfo().getId();
        String timeOption = request.getTimeOption();
        String expenseCategoriesId = request.getExpenseCategoriesId();
        String revenueCategoriesId = request.getRevenueCategoriesId();
        String start = null;
        String end = null;
        if(request.getCustomTimeRange() != null && request.getCustomTimeRange().size() > 0){
            start = request.getCustomTimeRange().get(0);
            end = request.getCustomTimeRange().get(1);
        }
        LocalDate startDate = null;
        LocalDate endDate = null;

        if(timeOption.equals(ReportTimeOptionType.MONTH.getType())){
            YearMonth startMonth = YearMonth.parse(start);
            YearMonth endMonth = YearMonth.parse(end);
            startDate = startMonth.atDay(1);
            endDate = endMonth.atEndOfMonth();
        }else if (timeOption.equals(ReportTimeOptionType.YEAR.getType())){
            int startYear = Integer.parseInt(start);
            int endYear = Integer.parseInt(end);
            startDate = LocalDate.of(startYear, 1, 1);
            endDate = LocalDate.of(endYear, 12, 31);
        } else if (timeOption.equals(ReportTimeOptionType.OPTIONAL.getType())) {
            startDate = LocalDate.parse(start);
            endDate = LocalDate.parse(end);
        }
        return reportExpenseRevenueRepository.getCategoryReportNoPaging(userId, expenseCategoriesId, revenueCategoriesId, startDate, endDate);
    }

}
