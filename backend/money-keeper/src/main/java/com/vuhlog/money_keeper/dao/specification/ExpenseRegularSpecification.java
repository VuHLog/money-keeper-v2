package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.ExpenseRegular;
import org.springframework.data.jpa.domain.Specification;

public class ExpenseRegularSpecification {
    public static Specification<ExpenseRegular> hasDictionaryBucketPaymentId(String dbpId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dictionaryBucketPayment"), dbpId);
    }

    public static Specification<ExpenseRegular> hasDictionaryExpenseId(String dictionaryExpenseId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dictionaryExpense"), dictionaryExpenseId);
    }

    public static Specification<ExpenseRegular> hasTripEventId(String tripEventId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("tripEvent"), tripEventId);
    }

    public static Specification<ExpenseRegular> hasBeneficiaryId(String beneficiaryId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("beneficiary"), beneficiaryId);
    }
}
