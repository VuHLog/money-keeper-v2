package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.constants.DictionaryBucketPaymentType;
import com.vuhlog.money_keeper.dao.*;
import com.vuhlog.money_keeper.dao.httpClient.CurrencyClient;
import com.vuhlog.money_keeper.dao.specification.DictionaryBucketPaymentSpecification;
import com.vuhlog.money_keeper.dto.request.BucketPaymentUsageStatus;
import com.vuhlog.money_keeper.dto.request.DictionaryBucketPaymentRequest;
import com.vuhlog.money_keeper.dto.request.ExpenseRevenueHistoryRequest;
import com.vuhlog.money_keeper.dto.response.DictionaryBucketPaymentResponse;
import com.vuhlog.money_keeper.dto.response.ExchangeRateResponse;
import com.vuhlog.money_keeper.dto.response.ExpenseRevenueHistory;
import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import com.vuhlog.money_keeper.entity.ExpenseRegular;
import com.vuhlog.money_keeper.entity.RevenueRegular;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.DictionaryBucketPaymentMapper;
import com.vuhlog.money_keeper.model.PeriodOfTime;
import com.vuhlog.money_keeper.service.DictionaryBucketPaymentService;
import com.vuhlog.money_keeper.util.TimestampUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictionaryBucketPaymentServiceImpl implements DictionaryBucketPaymentService {
    private final DictionaryBucketPaymentRepository dictionaryBucketPaymentRepository;
    private final ReportExpenseRevenueRepository reportExpenseRevenueRepository;
    private final ExpenseRegularRepository expenseRegularRepository;
    private final RevenueRegularRepository revenueRegularRepository;
    private final ExpenseLimitRepository expenseLimitRepository;
    private final BankRepository bankRepository;
    private final UsersRepository usersRepository;
    private final UserCommon userCommon;
    private final DictionaryBucketPaymentMapper dictionaryBucketPaymentMapper;
    private final CurrencyClient currencyClient;

    @PersistenceContext
    private EntityManager em;

    @Override
    public DictionaryBucketPaymentResponse createDictionaryBucketPayment(DictionaryBucketPaymentRequest request, String userId) {
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentMapper.toDictionaryBucketPayment(request);
        if(dictionaryBucketPayment.getAccountType().equalsIgnoreCase(DictionaryBucketPaymentType.BANK.getType())
        || dictionaryBucketPayment.getAccountType().equalsIgnoreCase(DictionaryBucketPaymentType.CREDIT_DEBIT_CARD.getType())){
            dictionaryBucketPayment.setBank(bankRepository.findById(request.getBankId()).orElseThrow(() -> new AppException(ErrorCode.BANK_NOT_EXISTED)));
        }
        dictionaryBucketPayment.setUser(usersRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
        dictionaryBucketPayment.setInitialBalance(request.getBalance());
        return dictionaryBucketPaymentMapper.toDictionaryBucketResponse(dictionaryBucketPaymentRepository.save(dictionaryBucketPayment));
    }

    @Override
    public DictionaryBucketPaymentResponse updateDictionaryBucketPayment(String id, DictionaryBucketPaymentRequest request) {
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        String oldCurrency = dictionaryBucketPayment.getCurrency();
        String newCurrency = request.getCurrency();
        dictionaryBucketPaymentMapper.updateDictionaryBucketPaymentFromRequest(request, dictionaryBucketPayment);
        ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate(newCurrency, oldCurrency, 1L);
        Double rate = exchangeRateResponse.getRate();
        dictionaryBucketPayment.setBalance(dictionaryBucketPayment.getBalance() * rate);
        dictionaryBucketPayment.setInitialBalance(dictionaryBucketPayment.getInitialBalance() * rate);
        if(dictionaryBucketPayment.getAccountType().equalsIgnoreCase(DictionaryBucketPaymentType.BANK.getType())
                || dictionaryBucketPayment.getAccountType().equalsIgnoreCase(DictionaryBucketPaymentType.CREDIT_DEBIT_CARD.getType())){
            dictionaryBucketPayment.setBank(bankRepository.findById(request.getBankId()).orElseThrow(() -> new AppException(ErrorCode.BANK_NOT_EXISTED)));
        }
        return dictionaryBucketPaymentMapper.toDictionaryBucketResponse(dictionaryBucketPaymentRepository.save(dictionaryBucketPayment));
    }

    @Override
    public DictionaryBucketPaymentResponse updateUsageStatus(String id, BucketPaymentUsageStatus status) {
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        dictionaryBucketPayment.setHaveUse(status.isStatus());
        return dictionaryBucketPaymentMapper.toDictionaryBucketResponse(dictionaryBucketPaymentRepository.save(dictionaryBucketPayment));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDictionaryBucketPayment(String id) {
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BUCKET_PAYMENT_NOT_EXISTED));
        expenseRegularRepository.unsetBeneficiaryAccountInExpenseRegular(id);
        revenueRegularRepository.unsetSenderAccountInRevenueRegular(id);
        reportExpenseRevenueRepository.deleteByBucketPaymentId(id);
        expenseLimitRepository.deleteByBucketPaymentId(id);
        dictionaryBucketPaymentRepository.deleteById(id);
    }

    @Override
    public DictionaryBucketPaymentResponse getDictionaryBucketPaymentById(String id) {
        DictionaryBucketPayment dictionaryBucketPayment = dictionaryBucketPaymentRepository.findById(id).orElse(null);
        return dictionaryBucketPaymentMapper.toDictionaryBucketResponse(dictionaryBucketPayment);
    }

    @Override
    public List<DictionaryBucketPaymentResponse> getAllDictionaryBucketPayment(String userId, String search) {
        Specification<DictionaryBucketPayment> specs = Specification.where(null);
        specs = specs.and(DictionaryBucketPaymentSpecification.filterByUserId(userId));

        if (search != null && !search.isEmpty()) {
            specs = specs.and(DictionaryBucketPaymentSpecification.filterByName(search));
        }

        Sort sortable = Sort.by("accountName").ascending();
        return dictionaryBucketPaymentRepository.findAll(specs, sortable).stream().map(dictionaryBucketPaymentMapper::toDictionaryBucketResponse).toList();
    }

    @Override
    public Page<DictionaryBucketPaymentResponse> getDictionaryBucketPaymentPagination(String userId, String field, Integer pageNumber, Integer pageSize, String sort, String search) {
        Specification<DictionaryBucketPayment> specs = Specification.where(null);
        specs = specs.and(DictionaryBucketPaymentSpecification.filterByUserId(userId));

        if (search != null && !search.isEmpty()) {
            specs = specs.and(DictionaryBucketPaymentSpecification.filterByName(search));
        }

        Sort sortable = Sort.by(field).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);
        return dictionaryBucketPaymentRepository.findAll(specs, pageable).map(dictionaryBucketPaymentMapper::toDictionaryBucketResponse);
    }

    @Override
    public Long getTotalBalanceBySearch(String search) {
        Specification<DictionaryBucketPayment> specs = Specification.where(null);
        specs = specs.and(DictionaryBucketPaymentSpecification.filterByUserId(userCommon.getMyUserInfo().getId()));

        if (search != null && !search.isEmpty()) {
            specs = specs.and(DictionaryBucketPaymentSpecification.filterByName(search));
        }
        AtomicReference<Double> totalBalance = new AtomicReference<>(0.0);
        dictionaryBucketPaymentRepository.findAll(specs).forEach(dictionaryBucketPayment -> {
            ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
            Double rate = exchangeRateResponse.getRate();
            totalBalance.updateAndGet(v -> v + (dictionaryBucketPayment.getBalance() * rate));
        });
        return Math.round(totalBalance.get());
    }

    @Override
    public List<ExpenseRevenueHistory> getAllExpenseRevenueRegularsByIdAndDate(ExpenseRevenueHistoryRequest req, Integer pageNumber, Integer pageSize, String sort) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        String bucketPaymentId = req.getBucketPaymentId();
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);

        List<Object[]> results = dictionaryBucketPaymentRepository.getAllExpenseRevenueHistoryByBucketPaymentId(bucketPaymentId, periodOfTime != null ? periodOfTime.getStartDate() : null, periodOfTime != null ? periodOfTime.getEndDate() : null);
        return convertToExpenseRevenueHistory(results);
    }

    @Override
    public Long getTotalExpenseByBucketPaymentId(ExpenseRevenueHistoryRequest req) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        String bucketPaymentId = req.getBucketPaymentId();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<DictionaryBucketPayment> root = cq.from(DictionaryBucketPayment.class);
        Join<DictionaryBucketPayment, ExpenseRegular> expenseRegularJoin = root.join("expenseRegulars", JoinType.INNER);
        cq.select(cb.sum(expenseRegularJoin.get("amount")));
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);
        Predicate lessThanDate = null;
        Predicate greaterThanDate = null;
        List<Predicate> predicates = new ArrayList<>();
        if (periodOfTime != null) {
            if(periodOfTime.getStartDate() != null){
                greaterThanDate = cb.greaterThanOrEqualTo(expenseRegularJoin.get("expenseDate"), periodOfTime.getStartDate());
                predicates.add(greaterThanDate);
            }
            if(periodOfTime.getEndDate() != null){
                lessThanDate = cb.lessThanOrEqualTo(expenseRegularJoin.get("expenseDate"), periodOfTime.getEndDate());
                predicates.add(lessThanDate);
            }
        }
        if(bucketPaymentId != null){
            Predicate equalToBucketPaymentId = cb.equal(root.get("id"), bucketPaymentId);
            predicates.add(equalToBucketPaymentId);
        }else{
            Predicate equalToUserId = cb.equal(root.get("user").get("id"), userCommon.getMyUserInfo().getId());
            predicates.add(equalToUserId);
        }
        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0;
   }

    @Override
    public Long getTotalRevenueByBucketPaymentId(ExpenseRevenueHistoryRequest req) {
        String timeOption = req.getTimeOption();
        String startDate = req.getStartDate();
        String endDate = req.getEndDate();
        String bucketPaymentId = req.getBucketPaymentId();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<DictionaryBucketPayment> root = cq.from(DictionaryBucketPayment.class);
        Join<DictionaryBucketPayment, RevenueRegular> revenueRegularJoin = root.join("revenueRegulars", JoinType.INNER);
        cq.select(cb.sum(revenueRegularJoin.get("amount")));
        PeriodOfTime periodOfTime = TimestampUtil.handleTimeOption(timeOption, startDate, endDate);
        Predicate lessThanDate = null;
        Predicate greaterThanDate = null;
        List<Predicate> predicates = new ArrayList<>();
        if (periodOfTime != null) {
            if(periodOfTime.getStartDate() != null){
                greaterThanDate = cb.greaterThanOrEqualTo(revenueRegularJoin.get("revenueDate"), periodOfTime.getStartDate());
                predicates.add(greaterThanDate);
            }
            if(periodOfTime.getEndDate() != null){
                lessThanDate = cb.lessThanOrEqualTo(revenueRegularJoin.get("revenueDate"), periodOfTime.getEndDate());
                predicates.add(lessThanDate);
            }
        }
        if(bucketPaymentId != null){
            Predicate equalToBucketPaymentId = cb.equal(root.get("id"), bucketPaymentId);
            predicates.add(equalToBucketPaymentId);
        }else{
            Predicate equalToUserId = cb.equal(root.get("user").get("id"), userCommon.getMyUserInfo().getId());
            predicates.add(equalToUserId);
        }
        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        Long result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0;
    }

    @Override
    public Long getBalanceByBucketPaymentId(String bucketPaymentId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<DictionaryBucketPayment> root = cq.from(DictionaryBucketPayment.class);
        cq.select(root.get("balance"));
        cq.where(cb.equal(root.get("id"), bucketPaymentId));
        Long result = em.createQuery(cq).getSingleResult();
        return result != null ? result : 0;
    }

    @Override
    public Long getMyTotalBalance() {
        List<DictionaryBucketPayment> dictionaryBucketPayments = dictionaryBucketPaymentRepository.findByUser_id(userCommon.getMyUserInfo().getId());
        AtomicReference<Double> totalBalance = new AtomicReference<>(0.0);
        dictionaryBucketPayments.forEach(dictionaryBucketPayment -> {
            ExchangeRateResponse exchangeRateResponse = currencyClient.exchangeRate("VND", dictionaryBucketPayment.getCurrency(), 1L);
            Double rate = exchangeRateResponse.getRate();
            totalBalance.updateAndGet(v -> v + (dictionaryBucketPayment.getBalance() * rate));
        });
        return Math.round(totalBalance.get());
    }

    private List<ExpenseRevenueHistory> convertToExpenseRevenueHistory(List<Object[]> list) {
        return list.stream().map(obj ->
                new ExpenseRevenueHistory(
                        (String) obj[0], // id
                        (long) obj[1], //current balance
                        (String) TimestampUtil.timestampToString((Timestamp) obj[2]), // date
                        ((Number) obj[3]).longValue(), // amount
                        (String) obj[4], // iconUrl
                        (String) obj[5], // categoryName
                        (String) obj[6], // type
                        (String) obj[7],  // interpretation
                        (String) obj[8], //transfer type
                        (String) obj[9]
                )
        ).collect(Collectors.toList());
    }
}
