package com.vuhlog.money_keeper.dao;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;

public interface DictionaryBucketPaymentRepository
        extends JpaRepository<DictionaryBucketPayment, String>, JpaSpecificationExecutor<DictionaryBucketPayment> {
    @Query(
            value =
                    "SELECT er.id, er.balance, er.expense_date AS date, amount,de.icon_url, de.name AS category_name, 'expense' AS TYPE, er.interpretation, transfer_type, dbp.account_name FROM expense_regular er\n"
                            + "LEFT JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n"
                            + "LEFT JOIN dictionary_bucket_payment dbp ON dbp.id = er.beneficiary_account_id\n"
                            + "WHERE (:bucketPaymentId IS NULL OR er.dictionary_bucket_payment_id = :bucketPaymentId)\n"
                            + "AND (:startDate IS NULL OR er.expense_date >= :startDate)\n "
                            + "AND (:endDate IS NULL OR er.expense_date <= :endDate)\n "
                            + "UNION\n"
                            + "SELECT rr.id,rr.balance, rr.revenue_date AS date,amount, dr.icon_url, dr.name AS category_name, 'revenue' AS TYPE, rr.interpretation, transfer_type, dbp.account_name FROM revenue_regular rr\n"
                            + "LEFT JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n"
                            + "LEFT JOIN dictionary_bucket_payment dbp ON dbp.id = rr.sender_account_id\n"
                            + "WHERE (:bucketPaymentId IS NULL OR rr.dictionary_bucket_payment_id = :bucketPaymentId)\n"
                            + "AND (:startDate IS NULL OR rr.revenue_date >= :startDate)\n "
                            + "AND (:endDate IS NULL OR rr.revenue_date <= :endDate)\n "
                            + "ORDER BY date desc",
            nativeQuery = true)
    List<Object[]> getAllExpenseRevenueHistoryByBucketPaymentId(
            @Param("bucketPaymentId") String bucketPaymentId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);

    @Query("select dbp.balance from DictionaryBucketPayment dbp where dbp.id = :bucketPaymentId")
    long getBalanceByBucketPaymentId(@Param("bucketPaymentId") String bucketPaymentId);

    @Query(value = "SELECT balance\n" +
            "FROM (\n" +
            "\tSELECT id, balance, expense_date AS date\n" +
            "\tFROM expense_regular\n" +
            "\tWHERE dictionary_bucket_payment_id = :bucketPaymentId AND expense_date < :date\n" +
            "\tUNION\n" +
            "\tSELECT id, balance, revenue_date AS date\n" +
            "\tFROM revenue_regular\n" +
            "\tWHERE dictionary_bucket_payment_id = :bucketPaymentId AND revenue_date < :date\n" +
            "\tORDER BY DATE DESC\n" +
            "\tLIMIT 1\n" +
            ") AS NearestTransaction", nativeQuery = true)
    Long getNearestTransactionByBucketPaymentIdAndLessThanDate(@Param("bucketPaymentId") String bucketPaymentId, @Param("date") Timestamp date);

    @Query(value = "SELECT id, balance , amount, type\n" +
            "FROM (\n" +
            "\tSELECT id,  balance, amount, expense_date AS DATE, 'expense' AS TYPE\n" +
            "\tFROM expense_regular\n" +
            "\tWHERE dictionary_bucket_payment_id = :bucketPaymentId AND expense_date > :date\n" +
            "\tUNION\n" +
            "\tSELECT id,  balance, amount, revenue_date AS DATE, 'revenue' AS TYPE\n" +
            "\tFROM revenue_regular\n" +
            "\tWHERE dictionary_bucket_payment_id = :bucketPaymentId AND revenue_date > :date\n" +
            "\tORDER BY DATE ASC\n" +
            "\tLIMIT 1\n" +
            ") AS NearestTransaction", nativeQuery = true)
    Object[] getNearestTransactionByBucketPaymentIdAndGreaterThanDate(@Param("bucketPaymentId") String bucketPaymentId, @Param("date") Timestamp date);

    @Query(
            value = "SELECT * FROM dictionary_bucket_payment\n" +
                    "WHERE user_id = :userId\n" +
                    "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(id, :bucketPaymentIds))",
            nativeQuery = true
    )
    List<DictionaryBucketPayment> findAllByIdIn(@Param("bucketPaymentIds") String bucketPaymentIds,@Param("userId") String userId);
}
