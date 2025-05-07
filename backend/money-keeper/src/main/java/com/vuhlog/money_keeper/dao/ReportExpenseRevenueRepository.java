package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.dto.response.responseinterface.dashboard.TotalExpenseRevenue;
import com.vuhlog.money_keeper.dto.response.responseinterface.report.*;
import com.vuhlog.money_keeper.entity.ReportExpenseRevenue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportExpenseRevenueRepository extends JpaRepository<ReportExpenseRevenue, String> {
    Optional<ReportExpenseRevenue> findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(int month, int year, String bucketPaymentId, String categoryId, String type);

    @Query(value = "SELECT COALESCE(SUM(total_expense), 0) AS totalExpense, COALESCE(SUM(total_revenue), 0) AS totalRevenue \n" +
            "FROM report_expense_revenue rer \n" +
            "WHERE rer.user_id = :userId \n" +
            "AND MONTH = MONTH(NOW()) AND YEAR = YEAR(NOW())", nativeQuery = true)
    TotalExpenseRevenue getTotalExpenseRevenueThisMonthByUserId(@Param("userId") String userId);

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

//    REPORT TRANSACTION TYPE
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

    @Query(value = "SELECT COALESCE(SUM(total_expense),0) AS totalExpense, COALESCE(SUM(total_revenue),0) AS totalRevenue, CONCAT(LPAD(rer.`month`, 2, '0'), '/', rer.`year`) AS `time`,COALESCE(SUM(total_transaction),0) AS totalTransaction\n" +
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


//    REPORT CATEGORY
    @Query(value = "SELECT COALESCE(SUM(amount),0) AS totalRevenue, dr.id as categoryId, dr.`name` AS categoryName, dr.icon_url AS iconUrl, DATE(revenue_date) AS time\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON rr.dictionary_revenue_id = dr.id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(rr.dictionary_revenue_id, :categoriesId))\n" +
            "and DATE(revenue_date) >= :startDate AND DATE(revenue_date) <= :endDate\n" +
            "GROUP BY DATE(revenue_date), dr.id, dr.`name`, dr.icon_url\n" +
            "ORDER BY DATE(revenue_date), dr.name", nativeQuery = true)
    List<ReportRevenueCategory> getReportRevenueCategoryByOptional(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT COALESCE(SUM(total_revenue),0) AS totalRevenue, dr.id as categoryId, dr.`name` AS categoryName, dr.icon_url AS iconUrl, CONCAT(LPAD(rer.`month`, 2, '0'), '/', rer.`year`) AS `time`\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_revenue dr ON rer.`type` = 'revenue' AND rer.category_id = dr.id\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(rer.category_id, :categoriesId))\n" +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startMonth AND :endMonth)\n" +
            "GROUP BY MONTH, YEAR, dr.id, dr.`name`, dr.icon_url\n" +
            "ORDER BY YEAR ASC, MONTH ASC, dr.name", nativeQuery = true)
    List<ReportRevenueCategory> getReportRevenueCategoryByMonth(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startMonth") LocalDate startMonth,
            @Param("endMonth") LocalDate endMonth
    );

    @Query(value = "SELECT COALESCE(SUM(total_revenue),0) AS totalRevenue, dr.id as categoryId, dr.`name` AS categoryName, dr.icon_url AS iconUrl, rer.`year` AS `time`\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_revenue dr ON rer.`type` = 'revenue' AND rer.category_id = dr.id\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(rer.category_id, :categoriesId))\n" +
            "AND year BETWEEN :startYear AND :endYear\n" +
            "GROUP BY YEAR, dr.id, dr.`name`, dr.icon_url\n" +
            "ORDER BY YEAR ASC, dr.name", nativeQuery = true)
    List<ReportRevenueCategory> getReportRevenueCategoryByYear(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startYear") int startYear,
            @Param("endYear") int endYear
    );

    @Query(value = "SELECT COALESCE(SUM(amount), 0) AS totalExpense, de.id as categoryId, de.`name` AS categoryName, de.icon_url AS iconUrl, DATE(expense_date) AS time\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id,:categoriesId))\n" +
            "and DATE(expense_date) >= :startDate AND DATE(expense_date) <= :endDate\n" +
            "GROUP BY DATE(expense_date), de.id, de.`name`, de.icon_url\n" +
            "ORDER BY DATE(expense_date), de.name", nativeQuery = true)
    List<ReportExpenseCategory> getReportExpenseCategoryByOptional(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT COALESCE(SUM(total_expense),0) AS totalExpense, de.id as categoryId, de.`name` AS categoryName, de.icon_url AS iconUrl, CONCAT(LPAD(rer.`month`, 2, '0'), '/', rer.`year`) AS `time`\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_expense de ON rer.`type` = 'expense' AND rer.category_id = de.id\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(rer.category_id,:categoriesId))\n" +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startMonth AND :endMonth)\n" +
            "GROUP BY MONTH, YEAR, de.id, de.`name`, de.icon_url\n" +
            "ORDER BY YEAR ASC, MONTH ASC, de.name", nativeQuery = true)
    List<ReportExpenseCategory> getReportExpenseCategoryByMonth(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startMonth") LocalDate startMonth,
            @Param("endMonth") LocalDate endMonth
    );

    @Query(value = "SELECT COALESCE(SUM(total_expense),0) AS totalExpense, de.id as categoryId, de.`name` AS categoryName, de.icon_url AS iconUrl, rer.`year` AS `time`\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_expense de ON rer.`type` = 'expense' AND rer.category_id = de.id\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND( :categoriesId IS NULL OR FIND_IN_SET(rer.category_id,:categoriesId))\n" +
            "AND year BETWEEN :startYear AND :endYear\n" +
            "GROUP BY YEAR, de.id, de.`name`, de.icon_url\n" +
            "ORDER BY YEAR ASC" +
            "ORDER BY YEAR ASC, de.name", nativeQuery = true)
    List<ReportExpenseCategory> getReportExpenseCategoryByYear(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startYear") int startYear,
            @Param("endYear") int endYear
    );


//    REPORT TRENDING
    @Query(value = "SELECT LPAD(dayOfMonth, 2, '0') AS dayOfMonth, total\n" +
            "FROM (\n" +
            "\tSELECT DAYOFMONTH(expense_date) AS dayOfMonth, COALESCE(SUM(amount),0) AS total\n" +
            "\tFROM expense_regular er\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tGROUP BY DAYOFMONTH(expense_date)\n" +
            "\tORDER BY DAYOFMONTH(expense_date)\n" +
            ") AS temp", nativeQuery = true)
    List<ReportDailyTrend> getReportExpenseDailyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT\n" +
            "    CASE \n" +
            "        WHEN dayOfWeek = 1 THEN 'Chủ nhật'\n" +
            "        WHEN dayOfWeek = 2 THEN 'Thứ 2'\n" +
            "        WHEN dayOfWeek = 3 THEN 'Thứ 3'\n" +
            "        WHEN dayOfWeek = 4 THEN 'Thứ 4'\n" +
            "        WHEN dayOfWeek = 5 THEN 'Thứ 5'\n" +
            "        WHEN dayOfWeek = 6 THEN 'Thứ 6'\n" +
            "        WHEN dayOfWeek = 7 THEN 'Thứ 7'\n" +
            "    END AS dayOfWeek,\n" +
            "    total\n" +
            "FROM\n" +
            "(\n" +
            "\tSELECT \n" +
            "\t    DAYOFWEEK(expense_date) AS dayOfWeek,\n" +
            "\t    COALESCE(SUM(amount), 0) AS total\n" +
            "\tFROM expense_regular er\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tGROUP BY DAYOFWEEK(expense_date)\n" +
            "\tORDER BY DAYOFWEEK(expense_date)\n" +
            ") AS temp", nativeQuery = true)
    List<ReportWeeklyTrend> getReportExpenseWeeklyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT CONCAT('Tháng ', MONTH) AS month, COALESCE(SUM(total_expense),0) AS total\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "GROUP BY month\n" +
            "ORDER BY month", nativeQuery = true)
    List<ReportMonthlyTrend> getReportExpenseMonthlyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT YEAR AS year, COALESCE(SUM(total_expense),0) AS total\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND YEAR >= YEAR(NOW()) - 4\n" +
            "GROUP BY year\n" +
            "ORDER BY YEAR", nativeQuery = true)
    List<ReportYearlyTrend> getReportExpenseYearlyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT LPAD(dayOfMonth, 2, '0') AS dayOfMonth, total\n" +
            "FROM (\n" +
            "\tSELECT DAYOFMONTH(revenue_date) AS dayOfMonth, COALESCE(SUM(amount),0) AS total\n" +
            "\tFROM revenue_regular rr\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tGROUP BY DAYOFMONTH(revenue_date)\n" +
            "\tORDER BY DAYOFMONTH(revenue_date)\n" +
            ") AS temp", nativeQuery = true)
    List<ReportDailyTrend> getReportRevenueDailyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT\n" +
            "    CASE \n" +
            "        WHEN dayOfWeek = 1 THEN 'Chủ nhật'\n" +
            "        WHEN dayOfWeek = 2 THEN 'Thứ 2'\n" +
            "        WHEN dayOfWeek = 3 THEN 'Thứ 3'\n" +
            "        WHEN dayOfWeek = 4 THEN 'Thứ 4'\n" +
            "        WHEN dayOfWeek = 5 THEN 'Thứ 5'\n" +
            "        WHEN dayOfWeek = 6 THEN 'Thứ 6'\n" +
            "        WHEN dayOfWeek = 7 THEN 'Thứ 7'\n" +
            "    END AS dayOfWeek,\n" +
            "    total\n" +
            "FROM\n" +
            "(\n" +
            "\tSELECT \n" +
            "\t    DAYOFWEEK(revenue_date) AS dayOfWeek,\n" +
            "\t    COALESCE(SUM(amount), 0) AS total\n" +
            "\tFROM revenue_regular rr\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tGROUP BY DAYOFWEEK(revenue_date)\n" +
            "\tORDER BY DAYOFWEEK(revenue_date)\n" +
            ") AS temp", nativeQuery = true)
    List<ReportWeeklyTrend> getReportRevenueWeeklyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT CONCAT('Tháng ', MONTH) AS month, COALESCE(SUM(total_revenue),0) AS total\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "GROUP BY month\n" +
            "ORDER BY MONTH\n", nativeQuery = true)
    List<ReportMonthlyTrend> getReportRevenueMonthlyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT YEAR AS year, COALESCE(SUM(total_revenue),0) AS total\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND YEAR >= YEAR(NOW()) - 4\n" +
            "GROUP BY year\n" +
            "ORDER BY year", nativeQuery = true)
    List<ReportYearlyTrend> getReportRevenueYearlyTrend(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );


//    REPORT BUCKET PAYMENT
    @Query(value = "SELECT id, account_name, account_type, balance, icon_url\n" +
            "FROM dictionary_bucket_payment dbp\n" +
            "WHERE dbp.user_id = :userId\n" +
            "ORDER BY account_name", nativeQuery = true)
    List<ReportBucketPaymentBalance> getReportBucketPaymentBalance(
            @Param("userId") String userId
    );

    @Query(value = "SELECT COALESCE(SUM(balance),0) AS totalBalance, account_type, icon_url\n" +
            "FROM dictionary_bucket_payment dbp\n" +
            "WHERE dbp.user_id = :userId\n" +
            "GROUP BY account_type, icon_url\n" +
            "ORDER BY account_type", nativeQuery = true)
    List<ReportBucketPaymentTypeBalance> getReportBucketPaymentTypeBalance(
            @Param("userId") String userId
    );

//    TRANSACTION HISTORY
    @Query(value = "SELECT er.id,'expense' AS transactionType ,er.amount, de.`name` AS categoryName, de.icon_url AS categoryIconUrl, dbp.account_name as accountName, dbp.icon_url AS bucketPaymentIconUrl, expense_date AS date, er.transfer_type as transferType, er.interpretation\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds)) \n" +
            "AND ( :startDate IS NULL OR :endDate IS NULL OR (date(expense_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))\n" +
            "UNION ALL\n" +
            "SELECT rr.id,'revenue' AS transactionType ,rr.amount, dr.`name` AS categoryName, dr.icon_url AS categoryIconUrl, dbp.account_name as accountName, dbp.icon_url AS bucketPaymentIconUrl, revenue_date AS date, rr.transfer_type as transferType, rr.interpretation\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds)) \n" +
            "AND ( :startDate IS NULL OR :endDate IS NULL OR (date(revenue_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))"
            , countQuery = "SELECT COUNT(*) FROM (" +
            "SELECT er.id " +
            "FROM expense_regular er " +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id " +
            "WHERE dbp.user_id = :userId " +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :bucketPaymentIds)) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (date(expense_date) BETWEEN DATE(:startDate) AND DATE(:endDate))) " +
            "UNION ALL " +
            "SELECT rr.id " +
            "FROM revenue_regular rr " +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id " +
            "WHERE dbp.user_id = :userId " +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds)) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (date(revenue_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))" +
            ") AS count_table", nativeQuery = true)
    Page<TransactionHistory> getAllExpenseRevenueHistory(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query(value = "SELECT er.id,'expense' AS transactionType ,er.amount, de.`name` AS categoryName, de.icon_url AS categoryIconUrl, dbp.account_name as accountName, dbp.icon_url AS bucketPaymentIconUrl, expense_date AS date, er.transfer_type as transferType, er.interpretation\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds)) \n" +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId)) \n" +
            "AND ( :startDate IS NULL OR :endDate IS NULL OR (date(expense_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))",
            countQuery ="SELECT count(er.id)" +
                    "FROM expense_regular er " +
                    "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id " +
                    "WHERE dbp.user_id = :userId " +
                    "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds)) " +
                    "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId)) \n" +
                    "AND (:startDate IS NULL OR :endDate IS NULL OR (date(expense_date) BETWEEN DATE(:startDate) AND DATE(:endDate))) ", nativeQuery = true)
    Page<TransactionHistory> getAllExpenseHistory(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query(value = "SELECT rr.id,'revenue' AS transactionType ,rr.amount, dr.`name` AS categoryName, dr.icon_url AS categoryIconUrl, dbp.account_name as accountName, dbp.icon_url AS bucketPaymentIconUrl, revenue_date AS date, rr.transfer_type as transferType, rr.interpretation\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND ( :bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds)) \n" +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(rr.dictionary_revenue_id, :categoriesId)) \n" +
            "AND ( :startDate IS NULL OR :endDate IS NULL OR (date(revenue_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))",
            countQuery = "SELECT count(rr.id)\n" +
                    "FROM revenue_regular rr \n" +
                    "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id \n" +
                    "WHERE dbp.user_id = :userId \n" +
                    "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds)) \n" +
                    "AND (:categoriesId IS NULL OR FIND_IN_SET(rr.dictionary_revenue_id, :categoriesId)) \n" +
                    "AND (:startDate IS NULL OR :endDate IS NULL OR (date(revenue_date) BETWEEN DATE(:startDate) AND DATE(:endDate)))"
            ,nativeQuery = true)
    Page<TransactionHistory> getAllRevenueHistory(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}