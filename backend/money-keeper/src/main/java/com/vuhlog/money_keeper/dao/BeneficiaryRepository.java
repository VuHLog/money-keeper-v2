package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, String> {
    List<Beneficiary> findAllByUser_Id(String userId);
}