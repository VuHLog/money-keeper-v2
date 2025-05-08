package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, String>, JpaSpecificationExecutor<Notification> {
    Long countAllByReadStatusAndUser_Id(Integer readStatus, String userId);

    @Modifying
    @Query(value = "update notification set read_status = :readStatus where user_id = :userId", nativeQuery = true)
    void updateAllReadStatus(@Param("readStatus") Integer readStatus, @Param("userId") String userId);
}