package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.ExpenseLimit;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ExpenseLimitSpecification {
    public static Specification<ExpenseLimit> filterByUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<ExpenseLimit> filterByName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }

    public static Specification<ExpenseLimit> filterByCategoriesId(String categoriesId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(criteriaBuilder.function("FIND_IN_SET", Integer.class, criteriaBuilder.literal(categoriesId), root.get("categoriesId")),0);
    }

    public static Specification<ExpenseLimit> filterByBucketPaymentIds(String bucketPaymentIds) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(criteriaBuilder.function("FIND_IN_SET", Integer.class, criteriaBuilder.literal(bucketPaymentIds), root.get("bucketPaymentIds")),0);
    }

    public static Specification<ExpenseLimit> equalDate(Integer day, Integer month, Integer year) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
//            if(year != null) {
//                predicates.add(criteriaBuilder.lessThanOrEqualTo(criteriaBuilder.function("YEAR", Integer.class, root.get("startDateLimit")), year));
//            }
//            if(month != null) {
//                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("MONTH", Integer.class, root.get("startDate")), month));
//            }
//            if(day != null) {
//                predicates.add(criteriaBuilder.equal(criteriaBuilder.function("DAY", Integer.class, root.get("startDate")), day));
//            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
