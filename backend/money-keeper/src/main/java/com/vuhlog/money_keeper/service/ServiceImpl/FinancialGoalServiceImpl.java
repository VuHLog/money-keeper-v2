package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.dao.DictionaryBucketPaymentRepository;
import com.vuhlog.money_keeper.dao.ExpenseRegularRepository;
import com.vuhlog.money_keeper.dao.FinancialGoalRepository;
import com.vuhlog.money_keeper.dao.UsersRepository;
import com.vuhlog.money_keeper.dao.specification.FinancialGoalSpecification;
import com.vuhlog.money_keeper.dto.request.FinancialGoalRequest;
import com.vuhlog.money_keeper.dto.response.FinancialGoalResponse;
import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import com.vuhlog.money_keeper.entity.FinancialGoal;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.FinancialGoalMapper;
import com.vuhlog.money_keeper.service.FinancialGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialGoalServiceImpl implements FinancialGoalService {
    private final FinancialGoalRepository financialGoalRepository;
    private final FinancialGoalMapper financialGoalMapper;
    private final UsersRepository usersRepository;
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final UserCommon userCommon;

    @Override
    public FinancialGoalResponse getFinancialGoalById(String id) {
        FinancialGoal financialGoal = financialGoalRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_GOAL_NOT_EXISTED));
        return financialGoalMapper.toFinancialGoalResponse(financialGoal);
    }

    @Override
    public List<FinancialGoalResponse> getAllFinancialGoal() {
        Specification<FinancialGoal> specs = Specification.where(null);
        Users user = userCommon.getMyUserInfo();
        specs = specs.and(FinancialGoalSpecification.filterByUserId(user.getId()));
        return financialGoalRepository.findAll(specs).stream().map(financialGoalMapper::toFinancialGoalResponse).collect(Collectors.toList());
    }

    @Override
    public Page<FinancialGoalResponse> getAllFinancialGoalPagination(String field, Integer pageNumber, Integer pageSize, String sort, String search, Integer status) {
        Specification<FinancialGoal> specs = Specification.where(null);
        Users user = userCommon.getMyUserInfo();
        specs = specs.and(FinancialGoalSpecification.filterByUserId(user.getId()));
        specs = specs.and(FinancialGoalSpecification.equalStatus(status));
        Sort sortable = sort.equals("ASC") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return financialGoalRepository.findAll(specs,pageable).map(financialGoalMapper::toFinancialGoalResponse);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinancialGoalResponse createFinancialGoal(FinancialGoalRequest request, String userId) {
        FinancialGoal financialGoal = financialGoalMapper.toFinancialGoal(request);
        DictionaryBucketPayment bucketPayment = dictionaryBucketPaymentRepository.findById(request.getBucketPaymentId()).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        financialGoal.setStatus(0);
        financialGoal.setCurrentAmount(0L);
        financialGoal.setBucketPayment(bucketPayment);
        financialGoal.setCurrency(bucketPayment.getCurrency());
        financialGoal.setCurrencySymbol(bucketPayment.getCurrencySymbol());
        Users user = usersRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        financialGoal.setUser(user);
        return financialGoalMapper.toFinancialGoalResponse(financialGoalRepository.save(financialGoal));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FinancialGoalResponse updateFinancialGoal(String id, FinancialGoalRequest request) {
        FinancialGoal financialGoal = financialGoalRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_GOAL_NOT_EXISTED));
        financialGoalMapper.updateFinancialGoal(financialGoal, request);
        return financialGoalMapper.toFinancialGoalResponse(financialGoalRepository.save(financialGoal));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFinancialGoal(String id) {
        FinancialGoal financialGoal = financialGoalRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_GOAL_NOT_EXISTED));
        financialGoalRepository.deleteById(id);
    }
}
