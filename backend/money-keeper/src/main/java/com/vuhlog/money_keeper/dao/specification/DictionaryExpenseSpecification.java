package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.DictionaryExpense;
import org.springframework.data.jpa.domain.Specification;

public class DictionaryExpenseSpecification {
    public static Specification<DictionaryExpense> equalParentId(String parentId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("parentId"), parentId);
    }

    public static Specification<DictionaryExpense> equalUserId(String userId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<DictionaryExpense> equalIsRegular(boolean regular) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("regular"), regular);
    }

    public static Specification<DictionaryExpense> equalSystemDefault(boolean isDefault) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("systemDefault"), isDefault);
    }

    public static Specification<DictionaryExpense> equalName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<DictionaryExpense> notEqualName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("name"), name);
    }

    public static Specification<DictionaryExpense> likeName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
}
