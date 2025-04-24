package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.TimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dao.*;
import com.vuhlog.money_keeper.dao.specification.ExpenseRegularSpecification;
import com.vuhlog.money_keeper.dto.request.ExpenseRegularRequest;
import com.vuhlog.money_keeper.dto.request.ReportCategoryResponse;
import com.vuhlog.money_keeper.dto.request.TransferRequest;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final TripEventRepository tripEventRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final ExpenseRegularMapper expenseRegularMapper;
    private final RevenueRegularRepository revenueRegularRepository;
    private final RevenueRegularMapper revenueRegularMapper;

    @Override
    public List<ExpenseRegularResponse> getAllMyExpenseRegular(String dictionaryBucketPaymentId) {
        Specification<ExpenseRegular> specs = Specification.where(null);
        specs = specs.and(ExpenseRegularSpecification.hasDictionaryBucketPaymentId(dictionaryBucketPaymentId));

        return expenseRegularRepository.findAll(specs).stream().map(expenseRegularMapper::toExpenseRegularResponse).toList();
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
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(request.getDictionaryExpenseId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        expenseRegular.setDictionaryExpense(dictionaryExpense);
        TripEvent tripEvent = null;
        if(request.getTripEventId() != null && !request.getTripEventId().isEmpty()) {
            tripEvent = tripEventRepository.findById(request.getTripEventId()).orElse(null);
        }
        expenseRegular.setTripEvent(tripEvent);
        Beneficiary beneficiary = null;
        if(request.getBeneficiaryId() != null && !request.getBeneficiaryId().isEmpty()) {
            beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId()).orElse(null);
        }
        expenseRegular.setBeneficiary(beneficiary);

        //update balance
        updateBalance(dictionaryBucketPayment, request.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //update report expense revenue
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), request.getDictionaryBucketPaymentId(), request.getAmount(), request.getDictionaryExpenseId());

        long balance =  getBalanceWhenCreate(dictionaryBucketPayment, expenseRegular.getExpenseDate(), request.getAmount());
        expenseRegular.setBalance(balance);
        expenseRegular = expenseRegularRepository.save(expenseRegular);

        //check expense limit
        List<ExpenseLimitNotification> expenseLimitNotifications = getOverExpenseLimit();
        if(!expenseLimitNotifications.isEmpty()) {
            notificationService.expenseLimitNotification(expenseLimitNotifications);
        }

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
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);

        DictionaryBucketPayment beneficiaryAccount = dictionaryBucketPaymentRepository.findById(request.getBeneficiaryAccountId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        expenseRegular.setBeneficiaryAccount(beneficiaryAccount);

        DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findByName("Chuyển khoản");
        expenseRegular.setDictionaryExpense(dictionaryExpense);

        //save revenue for received account
        RevenueRegular revenueRegular = revenueRegularMapper.toRevenueRegularFromTransferRequest(request);
        revenueRegular.setDictionaryBucketPayment(beneficiaryAccount);
        revenueRegular.setSenderAccount(dictionaryBucketPayment);
        revenueRegular.setTransferType(TransferType.TRANSFER.getType());
        revenueRegular.setExpenseRegularId(expenseRegular.getId());
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findByName("Chuyển khoản");
        revenueRegular.setDictionaryRevenue(dictionaryRevenue);

        //update balance for sender accocunt
        updateBalance(dictionaryBucketPayment, request.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //update balance for beneficiary account
        updateBalance(beneficiaryAccount, -request.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //save report for sender account
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), request.getDictionaryBucketPaymentId(), request.getAmount(), dictionaryExpense.getId());

        //save report for beneficiary account
        updateTotalRevenueForReportExpenseRevenue(expenseRegular.getExpenseDate(), request.getDictionaryBucketPaymentId(), request.getAmount(), dictionaryRevenue.getId());


        long balance =  getBalanceWhenCreate(dictionaryBucketPayment, expenseRegular.getExpenseDate(), request.getAmount());
        expenseRegular.setBalance(balance);
        expenseRegularRepository.save(expenseRegular);

        long balanceRevenue =  getBalanceWhenCreate(beneficiaryAccount, expenseRegular.getExpenseDate(), - request.getAmount());
        revenueRegular.setBalance(balanceRevenue);
        revenueRegularRepository.save(revenueRegular);


        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public ExpenseRegularResponse updateExpenseRegularFromTransferRequest(String id, TransferRequest request) {
//        //update expense
//        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
//        PeriodOfTime updateTimeLimit = TimestampUtil.getPeriodOfTime(TimeOptionType.LAST_30_DAYS.getType());
//        long oldAmount = expenseRegular.getAmount();
//        long newAmount = request.getAmount();
//        String oldBucketPaymentId = expenseRegular.getDictionaryBucketPayment().getId();
//        String newBucketPaymentId = request.getDictionaryBucketPaymentId();
//        Timestamp oldExpenseDate = expenseRegular.getExpenseDate();
//        Timestamp newExpenseDate = TimestampUtil.stringToTimestamp(request.getExpenseDate());
//        if(expenseRegular.getExpenseDate().before(updateTimeLimit.getStartDate()) || expenseRegular.getExpenseDate().after(updateTimeLimit.getEndDate())){
//            throw new AppException(ErrorCode.UPDATE_TIME_LIMIT);
//        }
//        expenseRegularMapper.updateExpenseRegularFromTransferRequest(request, expenseRegular);
//        expenseRegular.setBalance(expenseRegular.getBalance() - (newAmount - oldAmount));
//        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
//        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
//
//        DictionaryBucketPayment beneficiaryAccount = dictionaryBucketPaymentRepository.findById(request.getBeneficiaryAccountId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
//        expenseRegular.setBeneficiaryAccount(beneficiaryAccount);
//
//        expenseRegular = expenseRegularRepository.save(expenseRegular);
//
//        //update for beneficiary account
//        RevenueRegular revenueRegular = revenueRegularRepository.findByExpenseRegularId(expenseRegular.getId());
//        if(revenueRegular == null){
//            throw new AppException(ErrorCode.REVENUE_REGULAR_NOT_EXISTED);
//        }
//        revenueRegularMapper.updateRevenueRegularFromTransferRequest(request, revenueRegular);
//        revenueRegular.setDictionaryBucketPayment(beneficiaryAccount);
//        revenueRegular.setSenderAccount(dictionaryBucketPayment);
//        revenueRegular.setBalance(revenueRegular.getBalance() + newAmount - oldAmount);
//        revenueRegularRepository.save(revenueRegular);
//
//        //create transaction history
//        TransactionHistory transactionHistoryExpense = TransactionHistory.builder()
//                .transactionId(expenseRegular.getId())
//                .transactionType(TransactionType.EXPENSE.getType())
//                .oldAmount(oldAmount)
//                .newAmount(newAmount)
//                .senderAccountId(dictionaryBucketPayment.getId())
//                .beneficiaryAccountId(beneficiaryAccount.getId())
//                .build();
//
//        TransactionHistory transactionHistoryRevenue = TransactionHistory.builder()
//                .transactionId(expenseRegular.getBeneficiaryAccount().getId())
//                .transactionType(TransactionType.REVENUE.getType())
//                .oldAmount(oldAmount)
//                .newAmount(newAmount)
//                .senderAccountId(dictionaryBucketPayment.getId())
//                .beneficiaryAccountId(beneficiaryAccount.getId())
//                .build();
//        transactionHistoryRepository.saveAll(List.of(transactionHistoryExpense, transactionHistoryRevenue));
//
//        //update balance greater than date
//        if(oldAmount != newAmount){
//            updateBalance(dictionaryBucketPayment, (newAmount - oldAmount), expenseRegular.getExpenseDate(), null, true);
//            updateBalance(beneficiaryAccount, -(newAmount - oldAmount), revenueRegular.getRevenueDate(), null, true);
//        }
//
//        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
//    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExpenseRegularResponse updateExpenseRegular(String id, ExpenseRegularRequest request) {
        //save update expense
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        PeriodOfTime updateTimeLimit = TimestampUtil.getPeriodOfTime(TimeOptionType.LAST_30_DAYS.getType());
        long oldAmount = expenseRegular.getAmount();
        long newAmount = request.getAmount();
        String oldCategoryId = expenseRegular.getDictionaryExpense().getId();
        String newCategoryId = request.getDictionaryExpenseId();
        String oldBucketPaymentId = expenseRegular.getDictionaryBucketPayment().getId();
        String newBucketPaymentId = request.getDictionaryBucketPaymentId();
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
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        expenseRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        if(!expenseRegular.getDictionaryExpense().getId().equals(request.getDictionaryExpenseId())){
            DictionaryExpense dictionaryExpense = dictionaryExpenseRepository.findById(request.getDictionaryExpenseId()).orElse(null);
            expenseRegular.setDictionaryExpense(dictionaryExpense);
        }
        TripEvent tripEvent = null;
        if(request.getTripEventId() != null && !request.getTripEventId().isEmpty()) {
            tripEvent = tripEventRepository.findById(request.getTripEventId()).orElse(null);
        }
        expenseRegular.setTripEvent(tripEvent);
        Beneficiary beneficiary = null;
        if(request.getBeneficiaryId() != null && !request.getBeneficiaryId().isEmpty()) {
            beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId()).orElse(null);
        }
        expenseRegular.setBeneficiary(beneficiary);

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

        if(!oldCategoryId.equals(newCategoryId)){
            //update report expense revenue
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, oldBucketPaymentId, - oldAmount, oldCategoryId);
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, newBucketPaymentId, oldAmount, newCategoryId);
        }

        if(!oldBucketPaymentId.equals(newBucketPaymentId)){
            DictionaryBucketPayment old =  dictionaryBucketPaymentRepository.findById(oldBucketPaymentId).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
            updateBalance(old, -oldAmount, oldExpenseDate, null, true);
            updateBalance(dictionaryBucketPayment, oldAmount, oldExpenseDate, null, true);

            //update report expense revenue
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, oldBucketPaymentId, - oldAmount, newCategoryId);
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, newBucketPaymentId, oldAmount, newCategoryId);
        }

        if(newExpenseDate.after(oldExpenseDate)){
            //update balance for all expense, revenue greater than old date, less than new date
            updateBalance(dictionaryBucketPayment, - oldAmount, oldExpenseDate, newExpenseDate, false);

            expenseRegular.setBalance(getBalanceWhenCreate(dictionaryBucketPayment, newExpenseDate, oldAmount));
        }else {
            //update balance for all expense, revenue greater than new date, less than old date
            updateBalance(dictionaryBucketPayment, oldAmount, newExpenseDate, oldExpenseDate, false);

            expenseRegular.setBalance(getBalanceWhenCreate(dictionaryBucketPayment, newExpenseDate, oldAmount));
        }

        if((newExpenseDate.getMonth() != oldExpenseDate.getMonth()) || (newExpenseDate.getYear() != oldExpenseDate.getYear())){
            updateTotalExpenseForReportExpenseRevenue(oldExpenseDate, newBucketPaymentId, - oldAmount, newCategoryId);
            updateTotalExpenseForReportExpenseRevenue(newExpenseDate, newBucketPaymentId, oldAmount, newCategoryId);
        }

        if(oldAmount != newAmount){
            expenseRegular.setBalance(expenseRegular.getBalance() - (newAmount - oldAmount));
            updateBalance(dictionaryBucketPayment, (newAmount - oldAmount), expenseRegular.getExpenseDate(), null, true);
            updateTotalExpenseForReportExpenseRevenue(newExpenseDate, newBucketPaymentId, (newAmount - oldAmount), newCategoryId);
        }

        expenseRegular = expenseRegularRepository.save(expenseRegular);
        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Override
    public ExpenseRegularResponse getExpenseRegularById(String id) {
        ExpenseRegular expenseRegular = expenseRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.EXPENSE_REGULAR_NOT_EXISTED));
        return expenseRegularMapper.toExpenseRegularResponse(expenseRegular);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateTotalExpenseForReportExpenseRevenue(Timestamp date, String bucketPaymentId, long amount, String categoryId){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;

        int year = calendar.get(Calendar.YEAR);
        ReportExpenseRevenue reportExpenseRevenue;
        Optional<ReportExpenseRevenue> optionalReportExpenseRevenue = reportExpenseRevenueRepository.findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(month, year, bucketPaymentId, categoryId, "expense");
        if(optionalReportExpenseRevenue.isPresent()) {
            reportExpenseRevenue = optionalReportExpenseRevenue.get();
            reportExpenseRevenue.setTotalExpense(reportExpenseRevenue.getTotalExpense() + amount);
        }else {
            reportExpenseRevenue = ReportExpenseRevenue.builder()
                    .month(month)
                    .year(year)
                    .totalExpense(amount)
                    .totalRevenue(0)
                    .bucketPaymentId(bucketPaymentId)
                    .categoryId(categoryId)
                    .type("expense")
                    .user(userCommon.getMyUserInfo())
                    .build();
        }
        reportExpenseRevenueRepository.save(reportExpenseRevenue);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateTotalRevenueForReportExpenseRevenue(Timestamp date, String bucketPaymentId, long amount, String categoryId){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;

        int year = calendar.get(Calendar.YEAR);
        ReportExpenseRevenue reportExpenseRevenue;
        Optional<ReportExpenseRevenue> optionalReportExpenseRevenue = reportExpenseRevenueRepository.findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(month, year, bucketPaymentId, categoryId, "revenue");
        if(optionalReportExpenseRevenue.isPresent()) {
            reportExpenseRevenue = optionalReportExpenseRevenue.get();
            reportExpenseRevenue.setTotalRevenue(reportExpenseRevenue.getTotalRevenue() + amount);
        }else {
            reportExpenseRevenue = ReportExpenseRevenue.builder()
                    .month(getMonth(month))
                    .year(year)
                    .totalExpense(0)
                    .totalRevenue(amount)
                    .bucketPaymentId(bucketPaymentId)
                    .categoryId(categoryId)
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
    protected void updateBalance(DictionaryBucketPayment dictionaryBucketPayment, long amount, Timestamp startDate, Timestamp endDate, boolean isUpdateBucketPayment) {
        //update balance for account table
        if(isUpdateBucketPayment){
            long oldBalance = dictionaryBucketPayment.getBalance();
            long newBalance = oldBalance - amount;
            dictionaryBucketPayment.setBalance(newBalance);
            dictionaryBucketPaymentRepository.save(dictionaryBucketPayment);
        }

        //update expense,revenue balance after this expense
        expenseRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), -amount, startDate, endDate);
        revenueRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), -amount, startDate, endDate);
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
        updateBalance(dictionaryBucketPayment, -expenseRegular.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //update report
        updateTotalExpenseForReportExpenseRevenue(expenseRegular.getExpenseDate(), expenseRegular.getDictionaryBucketPayment().getId(), - expenseRegular.getAmount(), expenseRegular.getDictionaryExpense().getId());
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
        updateBalance(dictionaryBucketPayment, -expenseRegular.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //update balance for beneficiary account
        DictionaryBucketPayment beneficiaryAccount = expenseRegular.getBeneficiaryAccount();
        updateBalance(beneficiaryAccount, expenseRegular.getAmount(), expenseRegular.getExpenseDate(), null, true);

        //update report
        Timestamp date = expenseRegular.getExpenseDate();
        updateTotalExpenseForReportExpenseRevenue(date, expenseRegular.getDictionaryBucketPayment().getId(), - expenseRegular.getAmount(),expenseRegular.getDictionaryExpense().getId());
        updateTotalRevenueForReportExpenseRevenue(date, expenseRegular.getBeneficiaryAccount().getId(), - expenseRegular.getAmount(), expenseRegular.getDictionaryExpense().getId());
    }

    private long getBalanceWhenCreate(DictionaryBucketPayment dictionaryBucketPayment, Timestamp date, long amount) {
        Long oldBalance = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndLessThanDate(dictionaryBucketPayment.getId(), date);
        if(oldBalance != null){
            return oldBalance - amount;
        }else{
            Object[] result = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndGreaterThanDate(dictionaryBucketPayment.getId(), date);
            if(result != null && result.length > 0){
                Object[] object = (Object[]) result[0];
                if(object.length >=4){
                    NearestTransaction nearestTransaction = new NearestTransaction(
                            object[0].toString(),  //id
                            ((Number) object[1]).longValue(),  //balance
                            ((Number) object[2]).longValue(),  //amount
                            (String)object[3] //type
                    );
                    if(nearestTransaction.getType().equals(TransactionType.EXPENSE.getType())){
                        return nearestTransaction.getBalance() + nearestTransaction.getAmount();
                    }else if(nearestTransaction.getType().equals(TransactionType.REVENUE.getType())){
                        return nearestTransaction.getBalance() - nearestTransaction.getAmount();
                    }
                }
            }else
                return dictionaryBucketPayment.getBalance() - amount;
        }
        return 0;
    }

    private boolean isValidExpenseDate(Timestamp expenseDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDayStart = now.plusDays(1).toLocalDate().atStartOfDay();
        return expenseDate.toLocalDateTime().isBefore(nextDayStart);
    }

    private List<ExpenseLimitNotification> getOverExpenseLimit(){
        String userId = userCommon.getMyUserInfo().getId();
        List<ExpenseLimitNotification> expenseLimitNotifications = new ArrayList<>();
        List<ExpenseLimitNotification> expenseLimitNotificationsQuery = expenseRegularRepository.findOverExpenseLimitByUserAndExpense(userId);
        if(expenseLimitNotificationsQuery != null){
            expenseLimitNotifications.addAll(expenseLimitNotificationsQuery);
        }
        return expenseLimitNotifications;
    }
}
