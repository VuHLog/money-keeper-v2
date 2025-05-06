package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.DictionaryRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DictionaryRevenueRepository extends JpaRepository<DictionaryRevenue, String>, JpaSpecificationExecutor<DictionaryRevenue> {
    DictionaryRevenue findByNameAndUser_Id(String name, String userId);
}