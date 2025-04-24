package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {
}