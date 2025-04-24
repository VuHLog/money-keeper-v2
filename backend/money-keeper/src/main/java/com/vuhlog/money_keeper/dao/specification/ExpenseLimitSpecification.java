package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.ExpenseLimit;
import org.springframework.data.jpa.domain.Specification;

public class ExpenseLimitSpecification {
    public static Specification<ExpenseLimit> filterByUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<ExpenseLimit> filterByName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
}
