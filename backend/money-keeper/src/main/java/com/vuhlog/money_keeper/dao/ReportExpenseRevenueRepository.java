package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.dto.response.responseinterface.report.ReportTransactionTypeReponse;
import com.vuhlog.money_keeper.entity.ReportExpenseRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportExpenseRevenueRepository extends JpaRepository<ReportExpenseRevenue, String> {
    Optional<ReportExpenseRevenue> findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(int month, int year, String bucketPaymentId, String categoryId, String type);

    @Query(value = "SELECT SUM(totalExpense) as finalTotalExpense FROM (" +
            "SELECT COALESCE(SUM(amount), 0) as totalExpense " +
            "FROM expense_regular er " +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id " +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id " +
            "WHERE dbp.user_id = :userId " +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds)) " +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId)) " +
            "AND DATE(er.expense_date) >= :startDate AND DATE(er.expense_date) <= :endOfStartMonth " +
            "UNION ALL " +
            "SELECT COALESCE(SUM(amount), 0) as totalExpense " +
            "FROM expense_regular er " +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id " +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id " +
            "WHERE dbp.user_id = :userId " +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds)) " +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId)) " +
            "AND DATE(er.expense_date) >= :startDateOfMonthEndDate AND DATE(er.expense_date) <= :endDate " +
            "UNION ALL " +
            "SELECT COALESCE(SUM(rer.total_expense), 0) AS totalExpense " +
            "FROM report_expense_revenue rer " +
            "JOIN dictionary_expense de ON (rer.type = 'expense' AND rer.category_id = de.id) " +
            "WHERE rer.user_id = :userId " +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds)) " +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(rer.category_id, :categoriesId)) " +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startDateBetween AND :endDateBetween) " +
            ") AS combined",
            nativeQuery = true)
    Long getTotalExpenseByPeriodOfTime(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") LocalDate startDate,
            @Param("endOfStartMonth") LocalDate endOfStartMonth,
            @Param("startDateBetween") LocalDate startDateBetween,
            @Param("endDateBetween") LocalDate endDateBetween,
            @Param("startDateOfMonthEndDate") LocalDate startDateOfMonthEndDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT COALESCE(SUM(totalExpense), 0) AS totalExpense, COALESCE(SUM(totalRevenue), 0) AS totalRevenue, time, COALESCE(SUM(totalTransaction), 0) AS totalTransaction\n" +
            "FROM (\n" +
            "\tSELECT COALESCE(SUM(amount), 0) AS totalExpense, 0 AS totalRevenue, DATE(expense_date) AS time, COUNT(er.id) AS totalTransaction\n" +
            "\tFROM expense_regular er\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tand DATE(expense_date) >= :startDate AND DATE(expense_date) <= :endDate\n" +
            "\tGROUP BY DATE(expense_date)\n" +
            "\tUNION ALL\n" +
            "\tSELECT 0 AS totalExpense, COALESCE(SUM(amount), 0) AS totalRevenue, DATE(revenue_date) AS time, COUNT(rr.id) AS totalTransaction\n" +
            "\tFROM revenue_regular rr\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tand DATE(revenue_date) >= :startDate AND DATE(revenue_date) <= :endDate\n" +
            "\tGROUP BY DATE(revenue_date)\n" +
            ") AS combined\n" +
            "GROUP BY time", nativeQuery = true)
    List<ReportTransactionTypeReponse> getReportForTransactionTypeByOptional(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT COALESCE(SUM(total_expense),0) AS totalExpense, COALESCE(SUM(total_revenue),0) AS totalRevenue, CONCAT(rer.`month`, '/', rer.`year`) AS `time`,COALESCE(SUM(total_transaction),0) AS totalTransaction\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startMonth AND :endMonth)\n" +
            "GROUP BY MONTH, YEAR\n" +
            "ORDER BY YEAR ASC, MONTH ASC", nativeQuery = true)
    List<ReportTransactionTypeReponse> getReportForTransactionTypeByMonth(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startMonth") LocalDate startMonth,
            @Param("endMonth") LocalDate endMonth
    );

    @Query(value = "SELECT COALESCE(SUM(total_expense),0) AS totalExpense, COALESCE(SUM(total_revenue),0) AS totalRevenue, rer.`year` AS `time`,COALESCE(SUM(total_transaction),0) AS totalTransaction\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND year BETWEEN :startYear AND :endYear\n" +
            "GROUP BY YEAR\n" +
            "ORDER BY YEAR ASC", nativeQuery = true)
    List<ReportTransactionTypeReponse> getReportForTransactionTypeByYear(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startYear") int startYear,
            @Param("endYear") int endYear
    );
}