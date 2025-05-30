package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, String>, JpaSpecificationExecutor<FinancialGoal> {
    List<FinancialGoal> findByStatus(Integer status);
}