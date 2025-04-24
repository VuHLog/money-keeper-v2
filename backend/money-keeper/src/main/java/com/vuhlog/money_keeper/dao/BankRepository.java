package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, String> {
}