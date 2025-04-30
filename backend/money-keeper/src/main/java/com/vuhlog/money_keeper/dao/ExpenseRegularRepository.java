package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitDetailResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import com.vuhlog.money_keeper.dto.response.responseinterface.HistoryExpenseResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseByExpenseLimit;
import com.vuhlog.money_keeper.entity.ExpenseRegular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface ExpenseRegularRepository extends JpaRepository<ExpenseRegular, String>, JpaSpecificationExecutor<ExpenseRegular> {
    @Modifying
    @Transactional
    @Query("UPDATE ExpenseRegular e SET e.dictionaryExpense = NULL WHERE e.dictionaryExpense.id = :dictionaryExpenseId")
    void unsetDictionaryExpenseInExpenseRegular(@Param("dictionaryExpenseId") String dictionaryExpenseId);

    @Modifying
    @Transactional
    @Query("UPDATE ExpenseRegular e SET e.dictionaryBucketPayment = NULL WHERE e.dictionaryBucketPayment.id = :dictionaryBucketPaymentId")
    void unsetDictionaryBucketPaymentInExpenseRegular(@Param("dictionaryBucketPaymentId") String dictionaryBucketPaymentId);

//    @Modifying
//    @Transactional
//    @Query("UPDATE ExpenseRegular e SET e.tripEvent = NULL WHERE e.tripEvent.id = :tripEventId")
//    void unsetTripEventInExpenseRegular(@Param("tripEventId") String tripEventId);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE ExpenseRegular e SET e.beneficiary = NULL WHERE e.beneficiary.id = :beneficiaryId")
//    void unsetBeneficiaryInExpenseRegular(@Param("beneficiaryId") String beneficiaryId);

    @Modifying
    @Transactional
    @Query("UPDATE ExpenseRegular e set e.balance = e.balance + :amount\n" +
            "where e.dictionaryBucketPayment.id = :bucketPaymentId and (:startDate IS NULL OR e.expenseDate > :startDate) and (:endDate IS NULL OR e.expenseDate < :endDate)")
    void updateBalanceByDatetime(
            @Param("bucketPaymentId") String bucketPaymentId,
            @Param("amount") long amount,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );


    @Query(value ="SELECT SUM(er.amount) total_expense\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:startDate IS NULL OR er.expense_date >= :startDate) AND (:endDate IS NULL OR er.expense_date <= :endDate)\n" +
            "AND (:bucketPaymentIdsJoin IS NULL OR FIND_IN_SET(dbp.id, :bucketPaymentIdsJoin))\n" +
            "AND (:categoriesIdJoin IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesIdJoin))", nativeQuery = true)
    Long getTotalExpenseByMonthAndThisYear(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("bucketPaymentIdsJoin") String bucketPaymentIdsJoin,
            @Param("categoriesIdJoin") String categoriesIdJoin,
            @Param("userId") String userId
    );

    @Query(value ="SELECT SUM(er.amount) as total, 'expense' AS TYPE, de.id, de.name, de.icon_url\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:startDate IS NULL OR er.expense_date >= :startDate) AND (:endDate IS NULL OR er.expense_date <= :endDate)\n" +
            "AND (:bucketPaymentIdsJoin IS NULL OR FIND_IN_SET(dbp.id,:bucketPaymentIdsJoin))\n" +
            "GROUP BY er.dictionary_expense_id", nativeQuery = true)
    List<Object[]> getTotalExpenseByTimeAndCategory(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("bucketPaymentIdsJoin") String bucketPaymentIdsJoin,
            @Param("userId") String userId
    );

    @Query(value = "SELECT DATE(expense_date), SUM(amount)\n" +
            "FROM expense_regular\n" +
            "WHERE (:bucketPaymentIds IS NULL OR FIND_IN_SET(dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(dictionary_expense_id,:categoriesId))\n" +
            "AND expense_date >= :startDate AND (:endDate IS NULL OR expense_date <= :endDate) \n" +
            "GROUP BY DATE(expense_date)", nativeQuery = true)
    List<Object[]> getTotalExpenseFromStartDateToNowByCategoryAndBucketPayment(
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );

    @Query(value = "SELECT el.id as expenseLimitId, SUM(er.amount) as totalExpense\n" +
            "FROM users u\n" +
            "JOIN expense_limit el ON u.id = el.user_id\n" +
            "JOIN dictionary_bucket_payment dbp ON u.id = dbp.user_id\n" +
            "JOIN expense_regular er ON er.dictionary_bucket_payment_id = dbp.id\n" +
            "WHERE u.id = :userId\n" +
            "AND el.id = :expenseLimitId\n" +
            "AND FIND_IN_SET(dictionary_bucket_payment_id, el.bucket_payment_ids)\n" +
            "AND FIND_IN_SET(dictionary_expense_id,el.categories_id)\n" +
            "AND expense_date >= :startDate\n" +
            "AND (:endDate IS NULL OR expense_date <= :endDate)\n"
            , nativeQuery = true)
    TotalExpenseByExpenseLimit getTotalExpenseByUserIdGroupByExpenseLimit(
            @Param("userId") String userId,
            @Param("expenseLimitId") String expenseLimitId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );

    @Query(value = "SELECT el.id , el.name AS NAME, el.amount AS limit_amount, SUM(er.amount) \n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON er.dictionary_bucket_payment_id = dbp.id\n" +
            "JOIN users u ON dbp.user_id = u.id\n" +
            "JOIN expense_limit el ON u.id = el.user_id\n" +
            "WHERE u.id = :userId\n" +
            "AND FIND_IN_SET(er.dictionary_bucket_payment_id, el.bucket_payment_ids)\n" +
            "AND FIND_IN_SET(er.dictionary_expense_id,el.categories_id)\n" +
            "AND expense_date >= start_date_limit\n" +
            "AND expense_date <= end_date_limit\n" +
            "GROUP BY el.id, el.amount\n" +
            "HAVING SUM(er.amount) > limit_amount\n" +
            "ORDER BY el.name ASC", nativeQuery = true)
    List<ExpenseLimitNotification> findOverExpenseLimitByUserAndExpense(String userId);

    @Query(value = "SELECT de.name as name, de.icon_url as iconUrl, er.expense_date as expenseDate, er.amount as amount, dbp.account_name AS bucketPaymentName\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_expense de ON er.dictionary_expense_id = de.id\n" +
            "JOIN dictionary_bucket_payment dbp ON er.dictionary_bucket_payment_id = dbp.id\n" +
            "WHERE (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId))\n" +
            "AND DATE(expense_date) >= DATE(:startDate)\n" +
            "AND (DATE(:endDate) IS NULL OR DATE(expense_date) <= DATE(:endDate))\n" +
            "ORDER BY er.expense_date DESC ", nativeQuery = true)
    List<HistoryExpenseResponse> getHistoryExpenseRegular(
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );
}