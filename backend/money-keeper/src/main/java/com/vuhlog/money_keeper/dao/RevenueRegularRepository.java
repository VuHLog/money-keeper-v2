package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.RevenueRegular;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

public interface RevenueRegularRepository extends JpaRepository<RevenueRegular, String>, JpaSpecificationExecutor<RevenueRegular> {
    @Modifying
    @Transactional
    @Query("UPDATE RevenueRegular e SET e.dictionaryRevenue = NULL WHERE e.dictionaryRevenue.id = :dictionaryRevenueId")
    void unsetDictionaryRevenueInRevenueRegular(@Param("dictionaryRevenueId") String dictionaryRevenueId);

    @Modifying
    @Transactional
    @Query("UPDATE RevenueRegular e SET e.dictionaryBucketPayment = NULL WHERE e.dictionaryBucketPayment.id = :dictionaryBucketPaymentId")
    void unsetDictionaryBucketPaymentInRevenueRegular(@Param("dictionaryBucketPaymentId") String dictionaryBucketPaymentId);

    @Modifying
    @Transactional
    @Query("UPDATE RevenueRegular e SET e.senderAccount = NULL WHERE e.senderAccount.id = :dictionaryBucketPaymentId")
    void unsetSenderAccountInRevenueRegular(@Param("dictionaryBucketPaymentId") String dictionaryBucketPaymentId);

//    @Modifying
//    @Transactional
//    @Query("UPDATE RevenueRegular e SET e.tripEvent = NULL WHERE e.tripEvent.id = :tripEventId")
//    void unsetTripEventInRevenueRegular(@Param("tripEventId") String tripEventId);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE RevenueRegular e SET e.collectMoneyWho = NULL WHERE e.collectMoneyWho.id = :collectMoneyWhoId")
//    void unsetCollectMoneyWhoInRevenueRegular(@Param("collectMoneyWhoId") String collectMoneyWhoId);

    @Modifying
    @Transactional
    @Query("UPDATE RevenueRegular r set r.balance = r.balance + :amount, r.convertedBalance = r.convertedBalance + :convertedAmount\n" +
            "where r.dictionaryBucketPayment.id = :bucketPaymentId and (:startDate IS NULL OR r.revenueDate > :startDate) and (:endDate IS NULL OR r.revenueDate < :endDate)")
    void updateBalanceByDatetime(
            @Param("bucketPaymentId") String bucketPaymentId,
            @Param("amount") long amount,
            @Param("convertedAmount") long convertedAmount,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );

    @Query(value ="SELECT SUM(rr.amount) total_revenue\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:startDate IS NULL OR rr.revenue_date >= :startDate) AND (:endDate IS NULL OR rr.revenue_date <= :endDate)\n" +
            "AND (:bucketPaymentIdsJoin IS NULL OR FIND_IN_SET(dbp.id, :bucketPaymentIdsJoin))\n" +
            "AND (:categoriesIdJoin IS NULL OR FIND_IN_SET(rr.dictionary_revenue_id, :categoriesIdJoin))", nativeQuery = true)
    Long getTotalRevenueByMonthAndThisYear(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("bucketPaymentIdsJoin") String bucketPaymentIdsJoin,
            @Param("categoriesIdJoin") String categoriesIdJoin,
            @Param("userId") String userId
    );

    @Query(value ="SELECT SUM(rr.amount) as total, 'revenue' AS TYPE, dr.id, dr.name, dr.icon_url\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:startDate IS NULL OR rr.revenue_date >= :startDate) AND (:endDate IS NULL OR rr.revenue_date <= :endDate)\n" +
            "AND (:bucketPaymentIdsJoin IS NULL OR FIND_IN_SET(dbp.id,:bucketPaymentIdsJoin))\n" +
            "GROUP BY rr.dictionary_revenue_id", nativeQuery = true)
    List<Object[]> getTotalRevenueByTimeAndCategory(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("bucketPaymentIdsJoin") String bucketPaymentIdsJoin,
            @Param("userId") String userId
    );
}