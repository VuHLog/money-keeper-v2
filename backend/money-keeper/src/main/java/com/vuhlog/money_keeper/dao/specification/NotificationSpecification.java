package com.vuhlog.money_keeper.dao.specification;

import com.vuhlog.money_keeper.entity.Notification;
import org.springframework.data.jpa.domain.Specification;

public class NotificationSpecification {
    public static Specification<Notification> equalReadStatus(Integer readStatus) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("readStatus"), readStatus);
    }
}
