package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.DictionaryRevenue;
import org.springframework.data.jpa.domain.Specification;

public class DictionaryRevenueSpecification {
    public static Specification<DictionaryRevenue> equalParentId(String parentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("parentId"), parentId);
    }

    public static Specification<DictionaryRevenue> equalUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DictionaryRevenue> equalIsRegular(boolean regular) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("regular"), regular);
    }

    public static Specification<DictionaryRevenue> equalSystemDefault(boolean isDefault) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("systemDefault"), isDefault);
    }

    public static Specification<DictionaryRevenue> equalName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
    }
    public static Specification<DictionaryRevenue> notEqualName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("name"), name);
    }
}
