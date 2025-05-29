package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.FinancialGoal;
import org.springframework.data.jpa.domain.Specification;

public class FinancialGoalSpecification {
    public static Specification<FinancialGoal> filterByUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<FinancialGoal> filterByName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<FinancialGoal> equalStatus(Integer status) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
    }
}
