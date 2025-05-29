package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.FinancialGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, String>, JpaSpecificationExecutor<FinancialGoal> {
}