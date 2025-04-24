package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.DictionaryExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DictionaryExpenseRepository extends JpaRepository<DictionaryExpense, String>, JpaSpecificationExecutor<DictionaryExpense> {
    DictionaryExpense findByName(String name);

    @Query(
            value = "SELECT * FROM dictionary_expense\n" +
                    "WHERE (:categoriesId IS NULL OR FIND_IN_SET(id, :categoriesId))\n",
            nativeQuery = true
    )
    List<DictionaryExpense> findAllByIdIn(@Param("categoriesId") String categoriesId);
}