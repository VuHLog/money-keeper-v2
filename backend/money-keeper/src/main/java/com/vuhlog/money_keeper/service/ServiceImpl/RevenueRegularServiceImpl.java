package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.TimeOptionType;
import com.vuhlog.money_keeper.constants.TransactionType;
import com.vuhlog.money_keeper.constants.TransferType;
import com.vuhlog.money_keeper.dao.*;
import com.vuhlog.money_keeper.dao.httpClient.CurrencyClient;
import com.vuhlog.money_keeper.dao.specification.RevenueRegularSpecification;
import com.vuhlog.money_keeper.dto.request.RevenueRegularRequest;
import com.vuhlog.money_keeper.dto.response.ExchangeRateResponse;
import com.vuhlog.money_keeper.dto.response.RevenueRegularResponse;
import com.vuhlog.money_keeper.entity.*;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.RevenueRegularMapper;
import com.vuhlog.money_keeper.model.NearestTransaction;
import com.vuhlog.money_keeper.model.PeriodOfTime;
import com.vuhlog.money_keeper.service.NotificationService;
import com.vuhlog.money_keeper.service.RevenueRegularService;
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
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RevenueRegularServiceImpl implements RevenueRegularService {
    private final UserCommon userCommon;
    private final NotificationService notificationService;
    private final ReportExpenseRevenueRepository reportExpenseRevenueRepository;
    private final RevenueRegularRepository revenueRegularRepository;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final DictionaryRevenueRepository dictionaryRevenueRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    private final RevenueRegularMapper revenueRegularMapper;
    private final CurrencyClient currencyClient;

    @Override
    public List<RevenueRegularResponse> getAllMyRevenueRegular(String dictionaryBucketPaymentId) {
        Specification<RevenueRegular> specs = Specification.where(null);
        specs = specs.and(RevenueRegularSpecification.hasDictionaryBucketPaymentId(dictionaryBucketPaymentId));

        return revenueRegularRepository.findAll(specs).stream().map(revenueRegularMapper::toRevenueRegularResponse).toList();
    }

    @Override
    public Page<RevenueRegularResponse> getAllMyRevenueRegularPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search) {
        Specification<RevenueRegular> specs = Specification.where(null);
        String userId = userCommon.getMyUserInfo().getId();
        specs = specs.and(RevenueRegularSpecification.filterByUserId(userId));

        if(search != null && !search.isEmpty()) {

        }

        Sort sortable = sort.equals("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return revenueRegularRepository.findAll(specs, pageable).map(revenueRegularMapper::toRevenueRegularResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RevenueRegularResponse createRevenueRegular(RevenueRegularRequest request) {
        //save revenue
        RevenueRegular revenueRegular = revenueRegularMapper.toRevenueRegular(request);
        if(!isValidRevenueDate(revenueRegular.getRevenueDate())){
            throw new AppException(ErrorCode.DATE_LESS_THAN_TOMORROW);
        }
        revenueRegular.setTransferType(TransferType.NORMAL.getType());
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElse(null);
        revenueRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findById(request.getDictionaryRevenueId()).orElse(null);
        revenueRegular.setDictionaryRevenue(dictionaryRevenue);

        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        revenueRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        revenueRegular.setConvertedAmount(request.getAmount());
        revenueRegular.setAmount(Math.round(request.getAmount() * rate));
        revenueRegular.setExchangeRate(rate);

        //update balance
        updateBalance(dictionaryBucketPayment, request.getAmount(), rate, revenueRegular.getRevenueDate(), null, true);

        //update report expense revenue
        updateTotalRevenueReportExpenseRevenue(revenueRegular.getRevenueDate(), request.getDictionaryBucketPaymentId(), revenueRegular.getAmount(), request.getDictionaryRevenueId(), 1);

        Double convertedBalance =  getBalanceWhenCreate(dictionaryBucketPayment, revenueRegular.getRevenueDate(), revenueRegular.getConvertedAmount());
        revenueRegular.setConvertedBalance(convertedBalance);
        revenueRegular.setBalance(convertedBalance * rate);
        revenueRegular = revenueRegularRepository.save(revenueRegular);
        notificationService.revenueNotification(revenueRegular, true);
        return revenueRegularMapper.toRevenueRegularResponse(revenueRegular);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public RevenueRegularResponse updateRevenueRegular(String id, RevenueRegularRequest request) {
        RevenueRegular revenueRegular = revenueRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.REVENUE_REGULAR_NOT_EXISTED));
        PeriodOfTime updateTimeLimit = TimestampUtil.getPeriodOfTime(TimeOptionType.LAST_30_DAYS.getType());
        long oldAmount = revenueRegular.getConvertedAmount();
        long newAmount = request.getAmount();
        String oldCategoryId = revenueRegular.getDictionaryRevenue() != null ? revenueRegular.getDictionaryRevenue().getId() : null;
        String newCategoryId = request.getDictionaryRevenueId();

        String oldBucketPaymentId = revenueRegular.getDictionaryBucketPayment() != null ? revenueRegular.getDictionaryBucketPayment().getId() : null;
        DictionaryBucketPayment oldBucketPayment =  dictionaryBucketPaymentRepository.findById(oldBucketPaymentId).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        String newBucketPaymentId = request.getDictionaryBucketPaymentId();
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(request.getDictionaryBucketPaymentId()).orElse(null);

        ExchangeRateResponse exchangeRateOldBucketPayment = currencyClient.exchangeRate("VND", oldBucketPayment.getCurrency(), 1L);
        Double rateOldBucketPayment = exchangeRateOldBucketPayment.getRate();
        Long oldAmountInVND = Math.round(oldAmount * rateOldBucketPayment);
        ExchangeRateResponse exchangeRateNewBucketPayment = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rateNewBucketPayment = exchangeRateNewBucketPayment.getRate();
        Long newAmountInVND = Math.round(newAmount * rateNewBucketPayment);

        Timestamp oldRevenueDate = revenueRegular.getRevenueDate();
        Timestamp newRevenueDate = TimestampUtil.stringToTimestamp(request.getRevenueDate());
        if(oldRevenueDate.before(updateTimeLimit.getStartDate()) || oldRevenueDate.after(updateTimeLimit.getEndDate())){
            throw new AppException(ErrorCode.UPDATE_TIME_LIMIT);
        }
        if(newRevenueDate.before(updateTimeLimit.getStartDate()) || newRevenueDate.after(updateTimeLimit.getEndDate())){
            throw new AppException(ErrorCode.UPDATE_TIME_LIMIT);
        }
        revenueRegularMapper.updateRevenueRegularFromRequest(request, revenueRegular);
        if(!isValidRevenueDate(revenueRegular.getRevenueDate())){
            throw new AppException(ErrorCode.DATE_LESS_THAN_TOMORROW);
        }
        revenueRegular.setDictionaryBucketPayment(dictionaryBucketPayment);
        DictionaryRevenue dictionaryRevenue = dictionaryRevenueRepository.findById(request.getDictionaryRevenueId()).orElse(null);
        revenueRegular.setDictionaryRevenue(dictionaryRevenue);
        revenueRegular.setCurrency(dictionaryBucketPayment.getCurrency());
        revenueRegular.setConvertedAmount(request.getAmount());
        revenueRegular.setAmount(Math.round(request.getAmount() * rateNewBucketPayment));
        revenueRegular.setExchangeRate(rateNewBucketPayment);

        //create transaction history
        TransactionHistory transactionHistory = TransactionHistory.builder()
                .transactionId(revenueRegular.getId())
                .transactionType(TransactionType.REVENUE.getType())
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
                updateTotalRevenueReportExpenseRevenue(oldRevenueDate, oldBucketPaymentId, - oldAmount, oldCategoryId, -1);
            }
            updateTotalRevenueReportExpenseRevenue(oldRevenueDate, newBucketPaymentId, oldAmount, newCategoryId, 1);
        }

        if(oldBucketPaymentId == null || !oldBucketPaymentId.equals(newBucketPaymentId)){
            if(oldBucketPaymentId != null){
                updateBalance(oldBucketPayment, -oldAmount, rateOldBucketPayment, oldRevenueDate, null, true);
            }
            updateBalance(dictionaryBucketPayment, oldAmount, rateNewBucketPayment, oldRevenueDate, null, true);

            //update report expense revenue
            if(oldBucketPaymentId != null){
                updateTotalRevenueReportExpenseRevenue(oldRevenueDate, oldBucketPaymentId, - oldAmountInVND, newCategoryId, -1);
            }
            updateTotalRevenueReportExpenseRevenue(oldRevenueDate, newBucketPaymentId, oldAmountInVND, newCategoryId, 1);
        }

        if(newRevenueDate.after(oldRevenueDate)){
            //update balance for all expense, revenue greater than old date, less than new date
            updateBalance(dictionaryBucketPayment, - oldAmount, rateNewBucketPayment, oldRevenueDate, newRevenueDate, false);

            Double convertedBalance = getBalanceWhenCreate(dictionaryBucketPayment, newRevenueDate, oldAmount);
            revenueRegular.setConvertedBalance(convertedBalance);
            revenueRegular.setBalance(convertedBalance * rateNewBucketPayment);
        }else {
            //update balance for all expense, revenue greater than new date, less than old date
            updateBalance(dictionaryBucketPayment, oldAmount, rateNewBucketPayment, newRevenueDate, oldRevenueDate, false);

            Double convertedBalance = getBalanceWhenCreate(dictionaryBucketPayment, newRevenueDate, oldAmount);
            revenueRegular.setConvertedBalance(convertedBalance);
            revenueRegular.setBalance(convertedBalance * rateNewBucketPayment);
        }

        if((newRevenueDate.getMonth() != oldRevenueDate.getMonth()) || (newRevenueDate.getYear() != oldRevenueDate.getYear())){
            updateTotalRevenueReportExpenseRevenue(oldRevenueDate, newBucketPaymentId, - oldAmountInVND, newCategoryId,-1);
            updateTotalRevenueReportExpenseRevenue(newRevenueDate, newBucketPaymentId, oldAmountInVND, newCategoryId, 1);
        }

        if(oldAmount != newAmount){
            revenueRegular.setBalance(revenueRegular.getBalance() + (newAmountInVND - oldAmountInVND));
            revenueRegular.setConvertedBalance(revenueRegular.getConvertedBalance() + (newAmount - oldAmount));
            updateBalance(dictionaryBucketPayment, newAmount - oldAmount, rateNewBucketPayment, revenueRegular.getRevenueDate(), null, true);
            updateTotalRevenueReportExpenseRevenue(newRevenueDate, newBucketPaymentId, (newAmountInVND - oldAmountInVND), newCategoryId, 0);
        }

        revenueRegular = revenueRegularRepository.save(revenueRegular);
        notificationService.revenueNotification(revenueRegular, false);
        return revenueRegularMapper.toRevenueRegularResponse(revenueRegular);
    }

    @Override
    public RevenueRegularResponse getRevenueRegularById(String id) {
        RevenueRegular revenueRegular = revenueRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.REVENUE_REGULAR_NOT_EXISTED));
        return revenueRegularMapper.toRevenueRegularResponse(revenueRegular);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRevenueRegular(String id) {
        RevenueRegular revenueRegular = revenueRegularRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.REVENUE_REGULAR_NOT_EXISTED));
        transactionHistoryRepository.deleteAllByTransactionId(revenueRegular.getId());

        revenueRegularRepository.deleteById(id);

        //update balance
        DictionaryBucketPayment dictionaryBucketPayment = revenueRegular.getDictionaryBucketPayment();
        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
        Double rate = exchangeRateResponse.getRate();
        if (dictionaryBucketPayment != null) {
            updateBalance(dictionaryBucketPayment, - revenueRegular.getConvertedAmount(), rate, revenueRegular.getRevenueDate(), null, true);
        }

        //update report
        if(dictionaryBucketPayment != null && revenueRegular.getDictionaryRevenue() != null){
            updateTotalRevenueReportExpenseRevenue(revenueRegular.getRevenueDate(), revenueRegular.getDictionaryBucketPayment().getId(), - revenueRegular.getAmount(), revenueRegular.getDictionaryRevenue().getId(), -1);
        }
        notificationService.deleteRevenueNotification(revenueRegular);
    }

    @Transactional(rollbackFor = Exception.class)
    protected void updateTotalRevenueReportExpenseRevenue(Timestamp date, String bucketPaymentId, long amount, String categoryId, long incrementTotalTransaction) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH) + 1;

        int year = calendar.get(Calendar.YEAR);
        ReportExpenseRevenue reportExpenseRevenue;
        Optional<ReportExpenseRevenue> optionalReportExpenseRevenue = reportExpenseRevenueRepository.findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(month, year, bucketPaymentId, categoryId, "revenue");
        if(optionalReportExpenseRevenue.isPresent()) {
            reportExpenseRevenue = optionalReportExpenseRevenue.get();
            Long totalTransaction = reportExpenseRevenue.getTotalTransaction() + incrementTotalTransaction;
            reportExpenseRevenue.setTotalRevenue(reportExpenseRevenue.getTotalRevenue() + amount);
            reportExpenseRevenue.setTotalTransaction(totalTransaction);
        }else {
            reportExpenseRevenue = ReportExpenseRevenue.builder()
                    .month(month)
                    .year(year)
                    .totalExpense(0)
                    .totalRevenue(amount)
                    .bucketPaymentId(bucketPaymentId)
                    .categoryId(categoryId)
                    .totalTransaction(1L)
                    .type("revenue")
                    .user(userCommon.getMyUserInfo())
                    .build();
        }
        reportExpenseRevenueRepository.save(reportExpenseRevenue);
    }


    @Transactional(rollbackFor = Exception.class)
    protected void updateBalance(DictionaryBucketPayment dictionaryBucketPayment, long amount, Double rate, Timestamp startDate, Timestamp endDate, boolean isUpdateBucketPayment) {
        if(isUpdateBucketPayment){
            //update balance for account table
            Double oldBalance = dictionaryBucketPayment.getBalance();
            Double newBalance = oldBalance + amount;
            dictionaryBucketPayment.setBalance(newBalance);
            dictionaryBucketPaymentRepository.save(dictionaryBucketPayment);
        }

        Long convertAmountVND = Math.round(amount * rate);

        //update expense,revenue balance after this expense
        expenseRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), convertAmountVND, amount, startDate, endDate);
        revenueRegularRepository.updateBalanceByDatetime(dictionaryBucketPayment.getId(), convertAmountVND, amount, startDate, endDate);

    }

    private Double getBalanceWhenCreate(DictionaryBucketPayment dictionaryBucketPayment, Timestamp date, long amount) {
        Double oldBalance = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndLessThanDate(dictionaryBucketPayment.getId(), date);
        if(oldBalance != null){
            return oldBalance + amount;
        }else{
            Object[] result = dictionaryBucketPaymentRepository.getNearestTransactionByBucketPaymentIdAndGreaterThanDate(dictionaryBucketPayment.getId(), date);
            if(result != null && result.length > 0){
                Object[] object = (Object[]) result[0];
                if(object.length >=4){
                    NearestTransaction nearestTransaction = new NearestTransaction(
                            object[0].toString(),  //id
                            ((Number) object[1]).doubleValue(),  //converted balance
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
                return dictionaryBucketPayment.getInitialBalance() + amount;
            }
        }
        return 0.0;
    }

    private boolean isValidRevenueDate(Timestamp revenueDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextDayStart = now.plusDays(1).toLocalDate().atStartOfDay();
        return revenueDate.toLocalDateTime().isBefore(nextDayStart);
    }
}
