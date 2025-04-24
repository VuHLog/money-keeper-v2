package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.ExpenseRegular;
import com.vuhlog.money_keeper.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, String> {
    void deleteAllByTransactionId(String id);
}