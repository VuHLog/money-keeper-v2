package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitDetailResponse;
import com.vuhlog.money_keeper.entity.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, String>, JpaSpecificationExecutor<ExpenseLimit> {
    @Query(value = "SELECT de.name as name, de.icon_url as iconUrl, er.expense_date as expenseDate, er.amount as amount, dbp.account_name AS bucketPaymentName\n" +
            "FROM expense_regular er\n" +
            "JOIN dictionary_expense de ON er.dictionary_expense_id = de.id\n" +
            "JOIN dictionary_bucket_payment dbp ON er.dictionary_bucket_payment_id = dbp.id\n" +
            "WHERE (:bucketPaymentIds IS NULL OR FIND_IN_SET(er.dictionary_bucket_payment_id, :bucketPaymentIds))\n" +
            "AND (:categoriesId IS NULL OR FIND_IN_SET(er.dictionary_expense_id, :categoriesId))\n" +
            "AND DATE(expense_date) >= DATE(:startDate)\n" +
            "AND (DATE(:endDate) IS NULL OR DATE(expense_date) <= DATE(:endDate))\n" +
            "ORDER BY er.expense_date DESC ", nativeQuery = true)
    List<ExpenseLimitDetailResponse> getExpenseByExpenseLimitAndDate(
            @Param("bucketPaymentIds") String bucketPaymentIds,
            @Param("categoriesId") String categoriesId,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate
    );
}