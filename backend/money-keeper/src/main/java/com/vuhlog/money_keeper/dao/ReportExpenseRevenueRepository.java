package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueByOptional;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueByPresent;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueForCategory;
import com.vuhlog.money_keeper.dto.response.responseinterface.TotalExpenseRevenueForExpenseRevenueSituation;
import com.vuhlog.money_keeper.entity.ReportExpenseRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReportExpenseRevenueRepository extends JpaRepository<ReportExpenseRevenue, String> {
    Optional<ReportExpenseRevenue> findByMonthAndYearAndBucketPaymentIdAndCategoryIdAndType(int month, int year, String bucketPaymentId, String categoryId, String type);


    @Query(value = "SELECT SUM(rer.total_expense) AS total_expense, SUM(rer.total_revenue) AS total_revenue\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIdsJoin IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIdsJoin))\n" +
            "AND (:categoriesIdJoin IS NULL OR FIND_IN_SET(rer.category_id, :categoriesIdJoin))" +
            "AND (:startMonth IS NULL OR rer.month >= :startMonth) AND (:endMonth IS NULL OR rer.month <= :endMonth)\n" +
            "AND (:year IS NULL OR YEAR = :year)\n", nativeQuery = true)
    Object[] getTotalExpenseRevenueByMonthAndThisYear(
            @Param("startMonth")int startMonth,
            @Param("endMonth") int endMonth,
            @Param("year") int year,
            @Param("userId") String userId,
            @Param("bucketPaymentIdsJoin") String bucketPaymentIdsJoin,
            @Param("categoriesIdJoin") String categoriesIdJoin
    );

    @Query(value = "SELECT rer.month as unit, SUM(rer.total_expense) as totalExpense, SUM(rer.total_revenue) as totalRevenue\n" +
            "from report_expense_revenue rer\n" +
            "WHERE YEAR = :year\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "GROUP BY rer.month\n" +
            "ORDER BY rer.month asc", nativeQuery = true)
    List<TotalExpenseRevenueForExpenseRevenueSituation> getReportExpenseRevenueByYearOrderByMonth(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("year") Integer year
    );

    @Query(value = "SELECT rer.year as unit, SUM(rer.total_expense) AS totalExpense, SUM(rer.total_revenue) AS totalRevenue\n" +
            "from report_expense_revenue rer\n" +
            "WHERE (:endYear IS NULL OR  rer.year <= :endYear) AND (:startYear IS NULL OR rer.year >= :startYear )\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "GROUP BY rer.year\n" +
            "ORDER BY rer.year asc ", nativeQuery = true)
    List<TotalExpenseRevenueForExpenseRevenueSituation> getReportExpenseRevenueByYearOrderByYear(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear
    );

    @Query(value = "SELECT CEIL(rer.month / 3) AS unit, SUM(rer.total_expense) AS totalExpense, SUM(rer.total_revenue) AS totalRevenue\n" +
            "from report_expense_revenue rer\n" +
            "WHERE YEAR = :year\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "GROUP BY CEIL(rer.month / 3)\n" +
            "ORDER BY unit asc", nativeQuery = true)
    List<TotalExpenseRevenueForExpenseRevenueSituation> getReportExpenseRevenueByYearOrderByQuarter(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("year") Integer year
    );

    @Query(value = "SELECT \n" +
            "(SELECT COALESCE(SUM(amount),0)\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON er.dictionary_bucket_payment_id = dbp.id \n" +
            "where DATE(expense_date) = CURDATE()\n" +
            "AND dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS today_expense,\n" +
            "(SELECT COALESCE(SUM(amount), 0)\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON er.dictionary_bucket_payment_id = dbp.id \n" +
            "where YEARWEEK(expense_date) = YEARWEEK(CURDATE())\n" +
            "AND dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS week_expense,\n" +
            "(SELECT COALESCE(SUM(total_expense), 0)\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE year = YEAR(CURDATE()) AND month = MONTH(CURDATE())\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS month_expense,\n" +
            "(SELECT COALESCE(SUM(total_expense), 0)\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE year = YEAR(CURDATE()) AND CEIL(month / 3) = CEIL(MONTH(CURDATE()) / 3)" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS quarter_expense,\n" +
            "(SELECT COALESCE(SUM(total_expense), 0)\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE year = YEAR(CURDATE())\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS year_expense,\n" +
            "(SELECT COALESCE(SUM(amount),0)\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON rr.dictionary_bucket_payment_id = dbp.id \n" +
            "where DATE(revenue_date) = CURDATE()\n" +
            "AND dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS today_revenue,\n" +
            "(SELECT COALESCE(SUM(amount),0)\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON rr.dictionary_bucket_payment_id = dbp.id \n" +
            "where YEARWEEK(revenue_date) = YEARWEEK(CURDATE())\n" +
            "AND dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS week_revenue,\n" +
            "(SELECT COALESCE(SUM(total_revenue), 0)\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE year = YEAR(CURDATE()) AND month = MONTH(CURDATE())\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS month_revenue,\n" +
            "(SELECT COALESCE(SUM(total_revenue), 0)\n" +
            "FROM report_expense_revenue rer \n" +
            "WHERE year = YEAR(CURDATE()) AND CEIL(month / 3) = CEIL(MONTH(CURDATE()) / 3)\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS quarter_revenue,\n" +
            "(SELECT COALESCE(SUM(total_revenue), 0)\n" +
            "FROM report_expense_revenue rer\n" +
            "WHERE year = YEAR(CURDATE())\n" +
            "AND user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            ") AS year_revenue", nativeQuery = true)
    TotalExpenseRevenueByPresent getReportExpenseRevenueByPresent(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds
    );

    @Query(value = "SELECT\n" +
            "\tSUM(totalExpense) AS totalExpense,\n" +
            "   SUM(totalRevenue) AS totalRevenue  \n" +
            "FROM\n" +
            "(\n" +
            "\tSELECT COALESCE(SUM(amount), 0) AS totalExpense, 0 AS totalRevenue\n" +
            "\tFROM expense_regular er\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "\tand DATE(er.expense_date) >= :startDate AND DATE(er.expense_date) <= :endOfStartMonth\n" +
            "\tUNION ALL\n" +
            "\tSELECT COALESCE(SUM(amount), 0) AS totalExpense, 0 AS totalRevenue\n" +
            "\tFROM expense_regular er\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id,:bucketPaymentIds))\n" +
            "\tAND DATE(er.expense_date) >= :startDateOfMonthEndDate AND DATE(er.expense_date) <= :endDate\n" +
            "\tUNION ALL\n" +
            "\tSELECT 0 AS totalExpense, COALESCE(SUM(amount), 0) AS totalRevenue\n" +
            "\tFROM revenue_regular rr\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id,:bucketPaymentIds))\n" +
            "\tAND DATE(rr.revenue_date) >= :startDate AND DATE(rr.revenue_date) <= :endOfStartMonth\n" +
            "\tUNION ALL\n" +
            "\tSELECT 0 AS totalExpense, COALESCE(SUM(amount), 0) AS totalRevenue\n" +
            "\tFROM revenue_regular rr\n" +
            "\tJOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "\tWHERE dbp.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id,:bucketPaymentIds))\n" +
            "\tAND DATE(rr.revenue_date) >= :startDateOfMonthEndDate AND DATE(rr.revenue_date) <= :endDate\n" +
            "\tUNION ALL\n" +
            "\tSELECT COALESCE(SUM(total_expense), 0) AS totalExpense, COALESCE(SUM(total_revenue), 0) AS totalRevenue\n" +
            "\tFROM report_expense_revenue rer\n" +
            "\tWHERE rer.user_id = :userId\n" +
            "\tAND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id,:bucketPaymentIds))\n" +
            "\tAND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startDateBetween AND :endDateBetween)\n" +
            ") AS derived", nativeQuery = true)
    TotalExpenseRevenueByOptional getReportExpenseRevenueByOptional(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endOfStartMonth") LocalDate endOfStartMonth,
            @Param("startDateBetween") LocalDate startDateBetween,
            @Param("endDateBetween") LocalDate endDateBetween,
            @Param("startDateOfMonthEndDate") LocalDate startDateOfMonthEndDate,
            @Param("endDate") LocalDate endDate
            );

    @Query(value = "SELECT COALESCE(SUM(rer.total_expense), 0) AS totalExpense, 0 AS totalRevenue, rer.type, de.name, de.icon_url AS iconUrl\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_expense de ON (rer.type = 'expense' AND rer.category_id = de.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:month is null or rer.month = :month) AND (:quarter IS NULL OR CEIL(rer.month / 3) = :quarter) AND (:year IS NULL OR rer.year = :year)\n" +
            "GROUP BY rer.type, de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(rer.total_revenue), 0) AS totalRevenue, rer.type, dr.name, dr.icon_url\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_revenue dr ON (rer.type = 'revenue' AND rer.category_id = dr.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:month is null or rer.month = :month) AND (:quarter IS NULL OR CEIL(rer.month / 3) = :quarter) AND (:year IS NULL OR rer.year = :year)\n" +
            "GROUP BY rer.type, dr.id, dr.name, dr.icon_url", nativeQuery = true)
    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategory(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("month") Integer month,
            @Param("quarter") Integer quarter,
            @Param("year") Integer year
    );

    @Query(value = "SELECT COALESCE(SUM(er.amount), 0) AS totalExpense, 0 AS totalRevenue, 'expense' as type, de.name, de.icon_url AS iconUrl\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:startDate IS NULL OR er.expense_date >= :startDate) AND (:endDate IS NULL OR er.expense_date <= :endDate)\n" +
            "GROUP BY de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(rr.amount), 0) AS totalRevenue, 'revenue' as type, dr.name, dr.icon_url AS iconUrl\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:startDate IS NULL OR rr.revenue_date >= :startDate) AND (:endDate IS NULL OR rr.revenue_date <= :endDate)\n" +
            "GROUP BY dr.id, dr.name, dr.icon_url", nativeQuery = true)
    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByTodayOrThisWeek(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );

    @Query(value = "SELECT COALESCE(SUM(rer.total_expense), 0) AS totalExpense, 0 AS totalRevenue, rer.type, de.name, de.icon_url AS iconUrl\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_expense de ON (rer.type = 'expense' AND rer.category_id = de.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:startMonth IS NULL OR rer.month >= :startMonth) AND (:endMonth IS NULL OR rer.month <= :endMonth)\n" +
            "AND (:year IS NULL OR YEAR = :year) \n" +
            "GROUP BY rer.type, de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(rer.total_revenue), 0) AS totalRevenue, rer.type, dr.name, dr.icon_url\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_revenue dr ON (rer.type = 'revenue' AND rer.category_id = dr.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:startMonth IS NULL OR rer.month >= :startMonth) AND (:endMonth IS NULL OR rer.month <= :endMonth)\n" +
            "AND (:year IS NULL OR YEAR = :year) \n" +
            "GROUP BY rer.type, dr.id, dr.name, dr.icon_url", nativeQuery = true)
    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByThisMonthOrThisQuarterOrThisYear(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startMonth")Integer startMonth,
            @Param("endMonth") Integer endMonth,
            @Param("year") Integer year
    );

    @Query(value="SELECT COALESCE(SUM(amount), 0) AS totalExpense, 0 AS totalRevenue, 'expense' as type, de.name, de.icon_url AS iconUrl\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "and DATE(er.expense_date) >= :startDate AND DATE(er.expense_date) <= :endOfStartMonth\n" +
            "GROUP BY de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT COALESCE(SUM(amount), 0) AS totalExpense, 0 AS totalRevenue, 'expense' as type, de.name, de.icon_url AS iconUrl\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = er.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_expense de ON de.id = er.dictionary_expense_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND DATE(er.expense_date) >= :startDateOfMonthEndDate AND DATE(er.expense_date) <= :endDate\n" +
            "GROUP BY de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(amount), 0) AS totalRevenue, 'revenue' as type, dr.name, dr.icon_url AS iconUrl\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND DATE(rr.revenue_date) >= :startDate AND DATE(rr.revenue_date) <= :endOfStartMonth\n" +
            "GROUP BY dr.id, dr.name, dr.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(amount), 0) AS totalRevenue, 'revenue' as type, dr.name, dr.icon_url AS iconUrl\n" +
            "FROM revenue_regular rr\n" +
            "JOIN dictionary_bucket_payment dbp ON dbp.id = rr.dictionary_bucket_payment_id\n" +
            "JOIN dictionary_revenue dr ON dr.id = rr.dictionary_revenue_id\n" +
            "WHERE dbp.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rr.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND DATE(rr.revenue_date) >= :startDateOfMonthEndDate AND DATE(rr.revenue_date) <= :endDate\n" +
            "GROUP BY dr.id, dr.name, dr.icon_url\n" +
            "UNION ALL\n" +
            "SELECT COALESCE(SUM(rer.total_expense), 0) AS totalExpense, 0 AS totalRevenue, rer.type, de.name, de.icon_url AS iconUrl\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_expense de ON (rer.type = 'expense' AND rer.category_id = de.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startDateBetween AND :endDateBetween)\n" +
            "GROUP BY rer.type, de.id, de.name, de.icon_url\n" +
            "UNION ALL\n" +
            "SELECT 0 AS totalExpense, COALESCE(SUM(rer.total_revenue), 0) AS totalRevenue, rer.type, dr.name, dr.icon_url AS iconUrl\n" +
            "FROM report_expense_revenue rer\n" +
            "JOIN dictionary_revenue dr ON (rer.type = 'revenue' AND rer.category_id = dr.id)\n" +
            "WHERE rer.user_id = :userId\n" +
            "AND (:bucketPaymentIds IS NULL OR FIND_IN_SET(rer.bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (DATE(CONCAT(year, '-', LPAD(month, 2, '0'), '-01')) BETWEEN :startDateBetween AND :endDateBetween)\n" +
            "GROUP BY rer.type, dr.id, dr.name, dr.icon_url", nativeQuery = true)
    List<TotalExpenseRevenueForCategory> getTotalExpenseRevenueForCategoryByOptional(
            @Param("userId") String userId,
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("startDate") LocalDate startDate,
            @Param("endOfStartMonth") LocalDate endOfStartMonth,
            @Param("startDateBetween") LocalDate startDateBetween,
            @Param("endDateBetween") LocalDate endDateBetween,
            @Param("startDateOfMonthEndDate") LocalDate startDateOfMonthEndDate,
            @Param("endDate") LocalDate endDate
    );

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
}