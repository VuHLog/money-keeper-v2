package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.ExpenseLimitTimeEnum;
import com.vuhlog.money_keeper.dao.DictionaryBucketPaymentRepository;
import com.vuhlog.money_keeper.dao.DictionaryExpenseRepository;
import com.vuhlog.money_keeper.dao.ExpenseLimitRepository;
import com.vuhlog.money_keeper.dao.specification.ExpenseLimitSpecification;
import com.vuhlog.money_keeper.dto.request.ExpenseLimitRequest;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitDetailResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseLimitResponse;
import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import com.vuhlog.money_keeper.entity.ExpenseLimit;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.DictionaryBucketPaymentMapper;
import com.vuhlog.money_keeper.mapper.DictionaryExpenseMapper;
import com.vuhlog.money_keeper.mapper.ExpenseLimitMapper;
import com.vuhlog.money_keeper.service.ExpenseLimitService;
import com.vuhlog.money_keeper.util.TimestampUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseLimitServiceImpl implements ExpenseLimitService {
    private final UserCommon userCommon;
    private final ExpenseLimitRepository expenseLimitRepository;
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final DictionaryExpenseRepository dictionaryExpenseRepository;
    private final ExpenseLimitMapper expenseLimitMapper;
    private final DictionaryExpenseMapper dictionaryExpenseMapper;
    private final DictionaryBucketPaymentMapper dictionaryBucketPaymentMapper;

    @Override
    public ExpenseLimitResponse getExpenseLimitById(String id) {
        ExpenseLimit expenseLimit = expenseLimitRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EXPENSE_LIMIT_NOT_EXISTED));
        ExpenseLimitResponse expenseLimitResponse = expenseLimitMapper.toExpenseLimitResponse(expenseLimit);
        return convertToResponse(expenseLimitResponse, expenseLimit.getBucketPaymentIds(), expenseLimit.getCategoriesId(), expenseLimit.getEndDate());
    }

    @Override
    public List<ExpenseLimitDetailResponse> getExpenseLimitDetailById(String id, String startDate, String endDate) {
        ExpenseLimit expenseLimit = expenseLimitRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EXPENSE_LIMIT_NOT_EXISTED));
        Timestamp startTime = Timestamp.valueOf(startDate);
        Timestamp endTime = null;
        if(endDate != null && !endDate.isEmpty()) {
            endTime = Timestamp.valueOf(endDate);
        }
        return expenseLimitRepository.getExpenseByExpenseLimitAndDate(expenseLimit.getBucketPaymentIds(), expenseLimit.getCategoriesId(), startTime, endTime);
    }

    @Override
    public List<ExpenseLimitResponse> getAllExpenseLimit() {
        Specification<ExpenseLimit> specs = Specification.where(null);
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        specs = specs.and(ExpenseLimitSpecification.filterByUserId(userId));
        List<ExpenseLimit> expenseLimits = expenseLimitRepository.findAll(specs);
        return expenseLimits.stream().map(expenseLimit -> {
            ExpenseLimitResponse expenseLimitResponse = expenseLimitMapper.toExpenseLimitResponse(expenseLimit);
            return convertToResponse(expenseLimitResponse, expenseLimit.getBucketPaymentIds(), expenseLimit.getCategoriesId(), expenseLimit.getEndDate());
        }).collect(Collectors.toList());
    }

    @Override
    public ExpenseLimitResponse createExpenseLimit(ExpenseLimitRequest request) {
        ExpenseLimit expenseLimit = expenseLimitMapper.toExpenseLimit(request);
        expenseLimit.setUser(userCommon.getMyUserInfo());
        String endDateString = request.getEndDate();
        if (endDateString != null && !endDateString.isEmpty()) {
            expenseLimit.setEndDate(TimestampUtil.stringToTimestamp(endDateString));
            LocalDate endDateMinimum = validEndDate(expenseLimit.getRepeatTime(), expenseLimit.getStartDate(), expenseLimit.getEndDate());
            if (endDateMinimum != null) {
                throw new AppException(ErrorCode.EXPENSE_LIMIT_END_DATE_INVALID, endDateMinimum.toString());
            }
        }
        expenseLimitRepository.save(expenseLimit);
        return convertToResponse(expenseLimitMapper.toExpenseLimitResponse(expenseLimit), request.getBucketPaymentIds(), request.getCategoriesId(), expenseLimit.getEndDate());
    }

    @Override
    public ExpenseLimitResponse updateExpenseLimit(String id, ExpenseLimitRequest request) {
        ExpenseLimit expenseLimit = expenseLimitRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EXPENSE_LIMIT_NOT_EXISTED));
        expenseLimitMapper.updateExpenseLimit(expenseLimit, request);
        String endDateString = request.getEndDate();
        if (endDateString != null) {
            expenseLimit.setEndDate(TimestampUtil.stringToTimestamp(endDateString));
            LocalDate endDateMinimum = validEndDate(expenseLimit.getRepeatTime(), expenseLimit.getStartDate(), expenseLimit.getEndDate());
            if (endDateMinimum != null) {
                throw new AppException(ErrorCode.EXPENSE_LIMIT_END_DATE_INVALID, endDateMinimum.toString());
            }
        }
        expenseLimitRepository.save(expenseLimit);
        return convertToResponse(expenseLimitMapper.toExpenseLimitResponse(expenseLimit), request.getBucketPaymentIds(), request.getCategoriesId(),expenseLimit.getEndDate());
    }

    @Override
    public void deleteExpenseLimit(String id) {
        ExpenseLimit expenseLimit = expenseLimitRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.EXPENSE_LIMIT_NOT_EXISTED));
        expenseLimitRepository.delete(expenseLimit);
    }

    private ExpenseLimitResponse convertToResponse(ExpenseLimitResponse res, String bucketPaymentIds, String categoriesId, Timestamp endDate) {
        Users user = userCommon.getMyUserInfo();
        String userId = user.getId();
        if (bucketPaymentIds != null && !bucketPaymentIds.isEmpty()) {
            List<DictionaryBucketPayment> bucketPayments = dictionaryBucketPaymentRepository.findAllByIdIn(bucketPaymentIds, userId);
            res.setBucketPayments(bucketPayments.stream().map(dictionaryBucketPaymentMapper::toDictionaryBucketResponse).collect(Collectors.toList()));
        }
        if (categoriesId != null && !categoriesId.isEmpty()) {
            res.setCategories(dictionaryExpenseRepository.findAllByIdIn(categoriesId));
        }
        if (endDate != null && !endDate.toString().isEmpty()) {
            res.setEndDate(TimestampUtil.timestampToStringOnlyDate(endDate));
        }
        return res;
    }

    private LocalDate validEndDate(String repeatTime, Timestamp startTimestamp, Timestamp endTimestamp) {
        LocalDate startDate = startTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = endTimestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (repeatTime.equals(ExpenseLimitTimeEnum.EVERY_DAY.getType())) {
            return startDate.plusDays(1).isAfter(endDate) ? startDate.plusDays(1) : null;
        } else if (repeatTime.equals(ExpenseLimitTimeEnum.EVERY_WEEK.getType())) {
            return startDate.plusWeeks(1).isAfter(endDate) ? startDate.plusWeeks(1) : null;
        } else if (repeatTime.equals(ExpenseLimitTimeEnum.EVERY_MONTH.getType())) {
            return startDate.plusMonths(1).isAfter(endDate) ? startDate.plusMonths(1) : null;
        } else if (repeatTime.equals(ExpenseLimitTimeEnum.EVERY_QUARTER.getType())) {
            return startDate.plusMonths(3).isAfter(endDate) ? startDate.plusMonths(3) : null;
        } else if (repeatTime.equals(ExpenseLimitTimeEnum.EVERY_YEAR.getType())) {
            return startDate.plusYears(1).isAfter(endDate) ? startDate.plusYears(1) : null;
        }
        return null;
    }
}
