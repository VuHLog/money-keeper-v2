package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.RevenueRegular;
import org.springframework.data.jpa.domain.Specification;

public class RevenueRegularSpecification {
    public static Specification<RevenueRegular> hasDictionaryBucketPaymentId(String dbpId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dictionaryBucketPayment"), dbpId);
    }

    public static Specification<RevenueRegular> hasDictionaryRevenueId(String dictionaryRevenueId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("dictionaryRevenue"), dictionaryRevenueId);
    }

    public static Specification<RevenueRegular> hasTripEventId(String tripEventId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("tripEvent"), tripEventId);
    }

    public static Specification<RevenueRegular> hasCollectMoneyWhoId(String collectMoneyWhoId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("collectMoneyWho"), collectMoneyWhoId);
    }
}
