package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.TimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dao.*;
import com.vuhlog.money_keeper.dao.httpClient.CurrencyClient;
import com.vuhlog.money_keeper.dao.specification.ExpenseRegularSpecification;
 import com.vuhlog.money_keeper.dto.request.ExpenseRegularRequest;
import com.vuhlog.money_keeper.dto.request.TransferRequest;
import com.vuhlog.money_keeper.dto.response.ExchangeRateResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRegularResponse;
import com.vuhlog.money_keeper.dto.response.TotalExpenseByDateResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import com.vuhlog.money_keeper.entity.*;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.ExpenseRegularMapper;
import com.vuhlog.money_keeper.mapper.RevenueRegularMapper;
import com.vuhlog.money_keeper.model.NearestTransaction;
import com.vuhlog.money_keeper.model.PeriodOfTime;
import com.vuhlog.money_keeper.service.ExpenseRegularService;
import com.vuhlog.money_keeper.service.NotificationService;
import com.vuhlog.money_keeper.util.TimestampUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpenseRegularServiceImpl implements ExpenseRegularService {
    private final UserCommon userCommon;
    private final NotificationService notificationService;
    private final ReportExpenseRevenueRepository reportExpenseRevenueRepository;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final ExpenseLimitRepository expenseLimitRepository;
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final DictionaryExpenseRepository dictionaryExpenseRepository;
    private final DictionaryRevenueRepository dictionaryRevenueRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExpenseRegularMapper expenseRegularMapper;
    private final RevenueRegularRepository revenueRegularRepository;
    private final FinancialGoalRepository financialGoalRepository;
    private final RevenueRegularMapper revenueRegularMapper;
    private final CurrencyClient currencyClient;

    @Override
    public List<ExpenseRegularResponse> getAllMyExpenseRegular(String dictionaryBucketPaymentId) {
        Specification<ExpenseRegular> specs = Specification.where(null);
        specs = specs.and(ExpenseRegularSpecification.hasDictionaryBucketPaymentId(dictionaryBucketPaymentId));

        return expenseRegularRepository.findAll(specs).stream().map(expenseRegularMapper::toExpenseRegularResponse).toList();
    }

    @Override
    public Page<ExpenseRegularResponse> getAllMyExpenseRegularPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search) {
        Specification<ExpenseRegular> specs = Specification.where(null);
        String userId = userCommon.getMyUserInfo().getId();
        specs = specs.and(ExpenseRegularSpecification.filterByUserId(userId));

        if(search != null && !search.isEmpty()) {

        }

        Sort sortable = sort.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return expenseRegularRepository.findAll(specs, pageable).map(expenseRegularMapper::toExpenseRegularResponse);
    }

    @Override
    public List<ExpenseRegularResponse> getByFinancialGoal(String financialGoalId) {
        return expenseRegularRepository.findByFinancialGoal_id(financialGoalId).stream().map(expenseRegularMapper::toExpenseRegularResponse).toList();
    }

    @Override
    public Page<ExpenseRegularResponse> getByFinancialGoalPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search, String financialGoalId) {
        Specification<ExpenseRegular> specs = Specification.where(null);
        String userId = userCommon.getMyUserInfo().getId();
        specs = specs.and(ExpenseRegularSpecification.filterByUserId(userId));

        if(search != null && !search.isEmpty()) {

        }

        Sort sortable = sort.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);
        return expenseRegularRepository.findByFinancialGoal_id(financialGoalId, pageable).map(expenseRegularMapper::toExpenseRegularResponse);
    }

    @Override
    public List<TotalExpenseByDateResponse> getTotalExpenseByExpenseLimit(String expenseLimitId, String startDate, String endDate) {
        ExpenseLimit expenseLimit = expenseLimitRepository.findById(expenseLimitId).orElseThrow(() -> new AppException(ErrorCode.EXPENSE_LIMIT_NOT_EXISTED));
        Timestamp startTime = Timestamp.valueOf(startDate);
        Timestamp endTime = null;
        if(endDate != null && !endDate.isEmpty()) {
            endTime = Timestamp.valueOf(endDate);
        }
        List<Object[]> totalExpenseByDateListObject = expenseRegularRepository.getTotalExpenseFromStartDateToNowByCategoryAndBucketPayment(expenseLimit.getBucketPaymentIds(), expenseLimit.getCategoriesId(), startTime, endTime);
        return totalExpenseByDateListObject.stream().map(obj ->
                new TotalExpenseByDateResponse(
                        ((java.sql.Date) obj[0]).toLocalDate().toString(), // date
                        ((Number) obj[1]).longValue() // total expense
                )
        ).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseRegularResponse createExpenseRegular(ExpenseRegularRequest request) {
        //save expense
        ExpenseRegular expenseRegular = expenseRegularMapper.toExpenseRegular(request);
        if(!isValidExpenseDate(expenseRegular.getExpenseDate())){
            throw new AppException(ErrorCode.DATE_LESS_THAN_TOMORROW);
        }
        expenseRegular.setTransferType(TransferType.NORMAL.getType());
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        if(dictionaryBucketPayment.getBalance() - request.getAmount() < 0){
            throw new AppException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(request.getDictionaryExpenseId()).orElseThrow(() -> new AppException(ErrorCode.DICTIONARY_EXPENSE_NOT_EXISTED));
        expenseRegular.setDictionaryExpense(dictionaryExpense);

        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        expenseRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        expenseRegular.setConvertedAmount(request.getAmount());
        expenseRegular.setAmount(Math.round(request.getAmount() * rate));
        expenseRegular.setExchangeRate(rate);

        //update balance
        updateBalance(dictionaryBucketPayment, Math.round(expenseRegular.getConvertedAmount()), rate, expenseRegular.getExpenseDate(), null, true);

        //update report expense revenue
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), dictionaryBucketPayment, expenseRegular.getAmount(), expenseRegular.getConvertedAmount(), request.getDictionaryExpenseId(), 1);

        Double convertedBalance =  getBalanceWhenCreate(dictionaryBucketPayment, expenseRegular.getExpenseDate(), expenseRegular.getConvertedAmount());
        expenseRegular.setConvertedBalance(convertedBalance);
        expenseRegular.setBalance(convertedBalance * rate);
        expenseRegular.setCurrencySymbol(dictionaryBucketPayment.getCurrencySymbol());
        expenseRegular = expenseRegularRepository.save(expenseRegular);

        //check expense limit
        List<ExpenseLimitNotification> expenseLimitNotifications = getOverExpenseLimit(request.getDictionaryBucketPaymentId());
        if(!expenseLimitNotifications.isEmpty()) {
            notificationService.expenseLimitNotification(expenseLimitNotifications);
        }

        notificationService.expenseNotification(expenseRegular, true);

        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseRegularResponse createExpenseRegularForGoal(ExpenseRegularRequest request) {
        FinancialGoal financialGoal = financialGoalRepository.findById(request.getFinancialGoalId()).orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_GOAL_NOT_EXISTED));
        if(financialGoal.getCurrentAmount() + request.getAmount() >= financialGoal.getTargetAmount()){
            request.setAmount(financialGoal.getTargetAmount() - financialGoal.getCurrentAmount());
            financialGoal.setStatus(1);
        }
        financialGoal.setCurrentAmount(financialGoal.getCurrentAmount() + request.getAmount());
        financialGoalRepository.save(financialGoal);

        //save expense
        ExpenseRegular expenseRegular = expenseRegularMapper.toExpenseRegular(request);
        expenseRegular.setFinancialGoal(financialGoal);
        expenseRegular.setExpenseDate(new Timestamp(System.currentTimeMillis()));
        expenseRegular.setTransferType(TransferType.NORMAL.getType());
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        if(dictionaryBucketPayment.getBalance() - request.getAmount() < 0){
            throw new AppException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findByNameAndUser_Id("Phát triển bản thân",dictionaryBucketPayment.getUser().getId());
        expenseRegular.setDictionaryExpense(dictionaryExpense);

        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        expenseRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        expenseRegular.setConvertedAmount(request.getAmount());
        expenseRegular.setAmount(Math.round(request.getAmount() * rate));
        expenseRegular.setExchangeRate(rate);

        //update balance
        updateBalance(dictionaryBucketPayment, Math.round(expenseRegular.getConvertedAmount()), rate, expenseRegular.getExpenseDate(), null, true);

        //update report expense revenue
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), dictionaryBucketPayment, expenseRegular.getAmount(), expenseRegular.getConvertedAmount(), dictionaryExpense.getId(), 1);

        Double convertedBalance =  getBalanceWhenCreate(dictionaryBucketPayment, expenseRegular.getExpenseDate(), expenseRegular.getConvertedAmount());
        expenseRegular.setConvertedBalance(convertedBalance);
        expenseRegular.setBalance(convertedBalance * rate);
        expenseRegular.setCurrencySymbol(dictionaryBucketPayment.getCurrencySymbol());
        expenseRegular = expenseRegularRepository.save(expenseRegular);

        //check expense limit
        List<ExpenseLimitNotification> expenseLimitNotifications = getOverExpenseLimit(request.getDictionaryBucketPaymentId());
        if(!expenseLimitNotifications.isEmpty()) {
            notificationService.expenseLimitNotification(expenseLimitNotifications);
        }

        notificationService.expenseForGoalNotification(expenseRegular);

        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseRegularResponse createExpenseRegularFromTransferRequest(TransferRequest request) {
        // save expense
        ExpenseRegular expenseRegular = expenseRegularMapper.toExpenseRegularFromTransferRequest(request);
        if(!isValidExpenseDate(expenseRegular.getExpenseDate())){
            throw new AppException(ErrorCode.DATE_LESS_THAN_TOMORROW);
        }
        expenseRegular.setTransferType(TransferType.TRANSFER.getType());
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        if(dictionaryBucketPayment.getBalance() - request.getAmount() < 0){
            throw new AppException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);

        DictionaryBucketPayment beneficiaryAccount = dictionaryBucketPaymentRepository.findById(request.getBeneficiaryAccountId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        expenseRegular.setBeneficiaryAccount(beneficiaryAccount);

        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findByNameAndUser_Id("Chuyển khoản", dictionaryBucketPayment.getUser().getId());
        expenseRegular.setDictionaryExpense(dictionaryExpense);

        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        expenseRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        expenseRegular.setConvertedAmount(expenseRegular.getAmount());
        expenseRegular.setAmount(Math.round(request.getAmount() * rate));
        expenseRegular.setExchangeRate(rate);

        //save revenue for received account
        RevenueRegular revenueRegular = revenueRegularMapper.toRevenueRegularFromTransferRequest(request);
        revenueRegular.setCurrency(beneficiaryAccount.getCurrency());
        revenueRegular.setConvertedAmount(expenseRegular.getConvertedAmount());
        revenueRegular.setAmount(Math.round(request.getAmount() * rate));
        revenueRegular.setExchangeRate(rate);
        revenueRegular.setDictionaryBucketPayment(beneficiaryAccount);
        revenueRegular.setSenderAccount(dictionaryBucketPayment);
        revenueRegular.setTransferType(TransferType.TRANSFER.getType());
        revenueRegular.setExpenseRegularId(expenseRegular.getId());
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findByNameAndUser_Id("Chuyển khoản", beneficiaryAccount.getUser().getId());
        revenueRegular.setDictionaryRevenue(dictionaryRevenue);

        //update balance for sender accocunt
        updateBalance(dictionaryBucketPayment, Math.round(expenseRegular.getConvertedAmount()), rate, expenseRegular.getExpenseDate(), null, true);

        //update balance for beneficiary account
        updateBalance(beneficiaryAccount, -Math.round(expenseRegular.getConvertedAmount()), rate, expenseRegular.getExpenseDate(), null, true);

        //save report for sender account
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), dictionaryBucketPayment, expenseRegular.getAmount(), expenseRegular.getConvertedAmount(), dictionaryExpense.getId(), 1);

        //save report for beneficiary account
        updateTotalRevenueForReportExpenseRevenue(expenseRegular.getExpenseDate(), beneficiaryAccount, revenueRegular.getAmount(), revenueRegular.getConvertedAmount(), dictionaryRevenue.getId(), 1);


        Double convertedBalance =  getBalanceWhenCreate(dictionaryBucketPayment, expenseRegular.getExpenseDate(), expenseRegular.getConvertedAmount());
        expenseRegular.setConvertedBalance(convertedBalance);
        expenseRegular.setBalance(convertedBalance * rate);
        expenseRegular.setCurrencySymbol(dictionaryBucketPayment.getCurrencySymbol());
        expenseRegularRepository.save(expenseRegular);

        Double convertedBalanceRevenue =  getBalanceWhenCreate(beneficiaryAccount, expenseRegular.getExpenseDate(), - expenseRegular.getConvertedAmount());
        revenueRegular.setConvertedBalance(convertedBalanceRevenue);
        revenueRegular.setBalance(convertedBalanceRevenue * rate);
        revenueRegular.setCurrencySymbol(beneficiaryAccount.getCurrencySymbol());
        revenueRegularRepository.save(revenueRegular);

        //check expense limit
        List<ExpenseLimitNotification> expenseLimitNotifications = getOverExpenseLimit(request.getDictionaryBucketPaymentId());
        if(!expenseLimitNotifications.isEmpty()) {
            notificationService.expenseLimitNotification(expenseLimitNotifications);
        }

        notificationService.expenseTransferNotification(expenseRegular);
        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseRegularResponse updateExpenseRegular(String id, ExpenseRegularRequest request) {
        //save update expense
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        PeriodOfTime updateTimeLimit = TimestampUtil.getPeriodOfTime(TimeOptionType.LAST_30_DAYS.getType());
        long oldAmount = expenseRegular.getConvertedAmount();
        long newAmount = request.getAmount();
        String oldCategoryId = expenseRegular.getDictionaryExpense() != null ? expenseRegular.getDictionaryExpense().getId() : null;
        String newCategoryId = request.getDictionaryExpenseId();

        String oldBucketPaymentId = expenseRegular.getDictionaryBucketPayment() != null ? expenseRegular.getDictionaryBucketPayment().getId() : null;
        DictionaryBucketPayment oldBucketPayment =  dictionaryBucketPaymentRepository.findById(oldBucketPaymentId).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        String newBucketPaymentId = request.getDictionaryBucketPaymentId();
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));

        ExchangeRateResponse exchangeRateOldBucketPayment = currencyClient.exchangeRate("VND", oldBucketPayment.getCurrency(), 1L);
        Double rateOldBucketPayment = exchangeRateOldBucketPayment.getRate();
        Long oldAmountInVND = Math.round(oldAmount * rateOldBucketPayment);
        ExchangeRateResponse exchangeRateNewBucketPayment = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rateNewBucketPayment = exchangeRateNewBucketPayment.getRate();
        Long newAmountInVND = Math.round(newAmount * rateNewBucketPayment);

        Timestamp oldExpenseDate = expenseRegular.getExpenseDate();
        Timestamp newExpenseDate = TimestampUtil.stringToTimestamp(request.getExpenseDate());
        if(oldExpenseDate.before(updateTimeLimit.getStartDate()) || oldExpenseDate.after(updateTimeLimit.getEndDate())){
            throw new AppException(ErrorCode.UPDATE_TIME_LIMIT);
        }
        if(newExpenseDate.before(updateTimeLimit.getStartDate()) || newExpenseDate.after(updateTimeLimit.getEndDate())){
            throw new AppException(ErrorCode.UPDATE_TIME_LIMIT);
        }
        expenseRegularMapper.updateExpenseRegularFromRequest(request, expenseRegular);
        if(!isValidExpenseDate(expenseRegular.getExpenseDate())){
            throw new AppException(ErrorCode.DATE_LESS_THAN_TOMORROW);
        }
        if(dictionaryBucketPayment.getBalance() - (newAmount - oldAmount) < 0){
            throw new AppException(ErrorCode.NOT_ENOUGH_MONEY);
        }
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(request.getDictionaryExpenseId()).orElse(null);
        expenseRegular.setDictionaryExpense(dictionaryExpense);
        expenseRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        expenseRegular.setConvertedAmount(request.getAmount());
        expenseRegular.setAmount(Math.round(request.getAmount() * rateNewBucketPayment));
        expenseRegular.setExchangeRate(rateNewBucketPayment);
        expenseRegular.setCurrencySymbol(dictionaryBucketPayment.getCurrencySymbol());

        //create transaction history
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .transactionId(expenseRegular.getId())
                .transactionType(TransactionType.EXPENSE.getType())
                .oldAmount(oldAmount)
                .newAmount(newAmount)
                .oldCategoryId(oldCategoryId)
                .newCategoryId(newCategoryId)
                .bucketPayment(dictionaryBucketPayment)
                .build();
        transactionHistoryRepository.save(transactionHistory);

        if(oldCategoryId == null || !oldCategoryId.equals(newCategoryId)){
            //update report expense revenue
            if(oldCategoryId != null){
                updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, oldBucketPayment, - oldAmountInVND, - oldAmount, oldCategoryId, -1);
            }
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, oldBucketPayment, oldAmountInVND, oldAmount, newCategoryId, 1);
        }

        if(oldBucketPaymentId == null || !oldBucketPaymentId.equals(newBucketPaymentId)){
            if(oldBucketPaymentId != null){
                updateBalance(oldBucketPayment, -oldAmount, rateOldBucketPayment, oldExpenseDate, null, true);
            }
            updateBalance(dictionaryBucketPayment, oldAmount, rateNewBucketPayment, oldExpenseDate, null, true);

            //update report expense revenue
            if(oldBucketPaymentId != null){
                updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, oldBucketPayment, - oldAmountInVND, - oldAmount, newCategoryId, -1);
            }
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, dictionaryBucketPayment, oldAmountInVND, oldAmount, newCategoryId, 1);
        }

        if(newExpenseDate.after(oldExpenseDate)){
            //update balance for all expense, revenue greater than old date, less than new date
            updateBalance(dictionaryBucketPayment, - oldAmount, rateNewBucketPayment, oldExpenseDate, newExpenseDate, false);

            Double convertedBalance = getBalanceWhenCreate(dictionaryBucketPayment, newExpenseDate, oldAmount);
            expenseRegular.setConvertedBalance(convertedBalance);
            expenseRegular.setBalance(convertedBalance * rateNewBucketPayment);
        }else if(newExpenseDate.before(oldExpenseDate)) {
            //update balance for all expense, revenue greater than new date, less than old date
            updateBalance(dictionaryBucketPayment, oldAmount, rateNewBucketPayment, newExpenseDate, oldExpenseDate, false);

            Double convertedBalance = getBalanceWhenCreate(dictionaryBucketPayment, newExpenseDate, oldAmount);
            expenseRegular.setConvertedBalance(convertedBalance);
            expenseRegular.setBalance(convertedBalance * rateNewBucketPayment);
        }

        if((newExpenseDate.getMonth() != oldExpenseDate.getMonth()) || (newExpenseDate.getYear() != oldExpenseDate.getYear())){
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, dictionaryBucketPayment, - oldAmountInVND, - oldAmount, newCategoryId, -1);
            updateTotalExpenseForReportExpenseRevenue(newExpenseDate, dictionaryBucketPayment, oldAmountInVND, oldAmount, newCategoryId, 1);
        }

        if(oldAmount != newAmount){
            expenseRegular.setBalance(expenseRegular.getBalance() - (newAmountInVND - oldAmountInVND));
            expenseRegular.setConvertedBalance(expenseRegular.getConvertedBalance() - (newAmount - oldAmount));
            updateBalance(dictionaryBucketPayment, (newAmount - oldAmount), rateNewBucketPayment, expenseRegular.getExpenseDate(), null, true);
            updateTotalExpenseForReportExpenseRevenue(newExpenseDate, dictionaryBucketPayment, (newAmountInVND - oldAmountInVND), (newAmount - oldAmount), newCategoryId, 0);
        }

        expenseRegular = expenseRegularRepository.save(expenseRegular);

        //check expense limit
        List<ExpenseLimitNotification> expenseLimitNotifications = getOverExpenseLimit(request.getDictionaryBucketPaymentId());
        if(!expenseLimitNotifications.isEmpty()) {
            notificationService.expenseLimitNotification(expenseLimitNotifications);
        }
        notificationService.expenseNotification(expenseRegular, false);
        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Override
    public ExpenseRegularResponse getExpenseRegularById(String id) {
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateTotalExpenseForReportExpenseRevenue(Timestamp date, DictionaryBucketPayment bucketPayment, long amount, long convertedAmount, String categoryId, long incrementTotalTransaction){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;

        int year = calendar.get(Calendar.YEAR);
        ReportExpenseRevenue reportExpenseRevenue;
        Optional<ReportExpenseRevenue> optionalReportExpenseRevenue = reportExpenseRevenueRepository.findTop1ByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(month, year, bucketPayment.getId(), categoryId, "expense");
        if(optionalReportExpenseRevenue.isPresent()) {
            reportExpenseRevenue = optionalReportExpenseRevenue.get();
            reportExpenseRevenue.setTotalExpense(reportExpenseRevenue.getTotalExpense() + amount);
            reportExpenseRevenue.setConvertedTotalExpense(reportExpenseRevenue.getConvertedTotalExpense() + convertedAmount);
            Long totalTransaction = reportExpenseRevenue.getTotalTransaction() + incrementTotalTransaction;
            reportExpenseRevenue.setTotalTransaction(totalTransaction);
        }else {
            reportExpenseRevenue = ReportExpenseRevenue.builder()
                    .month(month)
                    .year(year)
                    .totalExpense(amount)
                    .convertedTotalExpense(convertedAmount)
                    .totalRevenue(0)
                    .convertedTotalRevenue(0)
                    .bucketPaymentId(bucketPayment.getId())
                    .currency(bucketPayment.getCurrency())
                    .currencySymbol(bucketPayment.getCurrencySymbol())
                    .categoryId(categoryId)
                    .type("expense")
                    .totalTransaction(1L)
                    .user(userCommon.getMyUserInfo())
                    .build();
        }
        reportExpenseRevenueRepository.save(reportExpenseRevenue);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateTotalRevenueForReportExpenseRevenue(Timestamp date, DictionaryBucketPayment bucketPayment, long amount, long convertedAmount, String categoryId, long incrementTotalTransaction){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;

        int year = calendar.get(Calendar.YEAR);
        ReportExpenseRevenue reportExpenseRevenue;
        Optional<ReportExpenseRevenue> optionalReportExpenseRevenue = reportExpenseRevenueRepository.findTop1ByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(month, year, bucketPayment.getId(), categoryId, "revenue");
        if(optionalReportExpenseRevenue.isPresent()) {
            reportExpenseRevenue = optionalReportExpenseRevenue.get();
            reportExpenseRevenue.setTotalRevenue(reportExpenseRevenue.getTotalRevenue() + amount);
            reportExpenseRevenue.setTotalRevenue(reportExpenseRevenue.getConvertedTotalRevenue() + convertedAmount);
            reportExpenseRevenue.setTotalTransaction(reportExpenseRevenue.getTotalTransaction() + incrementTotalTransaction);
        }else {
            reportExpenseRevenue = ReportExpenseRevenue.builder()
                    .month(getMonth(month))
                    .year(year)
                    .totalExpense(0)
                    .convertedTotalExpense(0)
                    .totalRevenue(amount)
                    .convertedTotalRevenue(convertedAmount)
                    .bucketPaymentId(bucketPayment.getId())
                    .currency(bucketPayment.getCurrency())
                    .currencySymbol(bucketPayment.getCurrencySymbol())
                    .categoryId(categoryId)
                    .totalTransaction(1L)
                    .type("revenue")
                    .user(userCommon.getMyUserInfo())
                    .build();
        }
        reportExpenseRevenueRepository.save(reportExpenseRevenue);
    }

    private static int getMonth(int month) {
        return month;
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateBalance(DictionaryBucketPayment dictionaryBucketPayment, long amount, Double rate, Timestamp startDate, Timestamp endDate, boolean isUpdateBucketPayment) {
        //update balance for account table
        if(isUpdateBucketPayment){
            Double oldBalance = dictionaryBucketPayment.getBalance();
            Double newBalance = oldBalance - amount;
            dictionaryBucketPayment.setBalance(newBalance);
            dictionaryBucketPaymentRepository.save(dictionaryBucketPayment);
        }

        Long convertAmountVND = Math.round(amount * rate);

        //update expense,revenue balance after this expense
        expenseRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), -convertAmountVND , -amount, startDate, endDate);
        revenueRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), -convertAmountVND, -amount, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpenseRegular(String id) {
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        //delete record in transaction history table
        transactionHistoryRepository.deleteAllByTransactionId(expenseRegular.getId());

        expenseRegularRepository.deleteById(id);

        //update balance
        DictionaryBucketPayment dictionaryBucketPayment = expenseRegular.getDictionaryBucketPayment();
        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        if(dictionaryBucketPayment != null){
            updateBalance(dictionaryBucketPayment, -expenseRegular.getConvertedAmount(), rate, expenseRegular.getExpenseDate(), null, true);
        }

        //update financial goal
        if(expenseRegular.getFinancialGoal() != null) {
            FinancialGoal financialGoal = expenseRegular.getFinancialGoal();
            financialGoal.setCurrentAmount(financialGoal.getCurrentAmount() - expenseRegular.getConvertedAmount());
            financialGoalRepository.save(financialGoal);
        }

        //update report
        if(expenseRegular.getDictionaryExpense() != null && dictionaryBucketPayment != null){
            updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), expenseRegular.getDictionaryBucketPayment(), - expenseRegular.getAmount(), - expenseRegular.getConvertedAmount(), expenseRegular.getDictionaryExpense().getId(), -1);
        }
        notificationService.deleteExpenseNotification(expenseRegular);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpenseRegularNoNotify(String id) {
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        //delete record in transaction history table
        transactionHistoryRepository.deleteAllByTransactionId(expenseRegular.getId());

        expenseRegularRepository.deleteById(id);

        //update balance
        DictionaryBucketPayment dictionaryBucketPayment = expenseRegular.getDictionaryBucketPayment();
        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        if(dictionaryBucketPayment != null){
            updateBalance(dictionaryBucketPayment, -expenseRegular.getConvertedAmount(), rate, expenseRegular.getExpenseDate(), null, true);
        }

        //update report
        if(expenseRegular.getDictionaryExpense() != null && dictionaryBucketPayment != null){
            updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), expenseRegular.getDictionaryBucketPayment(), - expenseRegular.getAmount(), - expenseRegular.getConvertedAmount(), expenseRegular.getDictionaryExpense().getId(), -1);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteExpenseRegularByTransferType(String id) {
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        //delete record in transaction history table
        transactionHistoryRepository.deleteAllByTransactionId(expenseRegular.getId());

        expenseRegularRepository.deleteById(id);

        //update balance for sender account
        DictionaryBucketPayment dictionaryBucketPayment = expenseRegular.getDictionaryBucketPayment();
        ExchangeRateResponse exchangeRateSender = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rateSender = exchangeRateSender.getRate();
        if(dictionaryBucketPayment != null){
            updateBalance(dictionaryBucketPayment, -expenseRegular.getConvertedAmount(), rateSender, expenseRegular.getExpenseDate(), null, true);
        }

        //update balance for beneficiary account
        DictionaryBucketPayment beneficiaryAccount = expenseRegular.getBeneficiaryAccount();
        ExchangeRateResponse exchangeRateBeneficiary = currencyClient.exchangeRate("VND", beneficiaryAccount.getCurrency(), 1L);
        Double rateBeneficiary = exchangeRateBeneficiary.getRate();
        if(beneficiaryAccount != null){
            updateBalance(beneficiaryAccount, expenseRegular.getConvertedAmount(), rateBeneficiary, expenseRegular.getExpenseDate(), null, true);
        }

        //update report
        Timestamp date = expenseRegular.getExpenseDate();
        if(dictionaryBucketPayment != null && expenseRegular.getDictionaryExpense() != null){
            updateTotalExpenseForReportExpenseRevenue(date, expenseRegular.getDictionaryBucketPayment(), - expenseRegular.getAmount(), - expenseRegular.getConvertedAmount(), expenseRegular.getDictionaryExpense().getId(), -1);
        }
        if(beneficiaryAccount != null && expenseRegular.getDictionaryExpense() != null){
            updateTotalRevenueForReportExpenseRevenue(date, expenseRegular.getBeneficiaryAccount(), - expenseRegular.getAmount(), -expenseRegular.getConvertedAmount(), expenseRegular.getDictionaryExpense().getId(), -1);
        }
    }

    private Double getBalanceWhenCreate(DictionaryBucketPayment dictionaryBucketPayment, Timestamp date, long amount) {
        Double oldBalance = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndLessThanDate(dictionaryBucketPayment.getId(), date);
        if(oldBalance != null){
            return oldBalance - amount;
        }else{
            Object[] result = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndGreaterThanDate(dictionaryBucketPayment.getId(), date);
            if(result != null && result.length > 0){
                Object[] object = (Object[]) result[0];
                if(object.length >=4){
                    NearestTransaction nearestTransaction = new NearestTransaction(
                            object[0].toString(),  //id
                            ((Number) object[1]).doubleValue(),  //converted balace
                            ((Number) object[2]).doubleValue(),  //amount
                            (String)object[3] //type
                    );
                    if(nearestTransaction.getType().equals(TransactionType.EXPENSE.getType())){
                        return nearestTransaction.getBalance() + nearestTransaction.getAmount();
                    }else if(nearestTransaction.getType().equals(TransactionType.REVENUE.getType())){
                        return nearestTransaction.getBalance() - nearestTransaction.getAmount();
                    }
                }
            }else{
                return dictionaryBucketPayment.getInitialBalance() - amount;
            }
        }
        return 0.0;
    }

    private boolean isValidExpenseDate(Timestamp expenseDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDayStart = now.plusDays(1).toLocalDate().atStartOfDay();
        return expenseDate.toLocalDateTime().isBefore(nextDayStart);
    }

    private List<ExpenseLimitNotification> getOverExpenseLimit(String bucketPaymentId){
        String userId = userCommon.getMyUserInfo().getId();
        List<ExpenseLimitNotification> expenseLimitNotifications = new ArrayList<>();
        List<ExpenseLimitNotification> expenseLimitNotificationsQuery = expenseRegularRepository.findOverExpenseLimitByUserAndExpense(userId, bucketPaymentId);
        if(expenseLimitNotificationsQuery != null){
            expenseLimitNotifications.addAll(expenseLimitNotificationsQuery);
        }
        return expenseLimitNotifications;
    }
}
