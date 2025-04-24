package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.DictionaryBucketPayment;
import org.springframework.data.jpa.domain.Specification;

public class DictionaryBucketPaymentSpecification {
    public static Specification<DictionaryBucketPayment> filterByUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DictionaryBucketPayment> filterByName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("accountName"), "%" + name + "%");
    }
}
