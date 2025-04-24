package com.vuhlog.money_keeper.dao;

import com.vuhlog.money_keeper.entity.TripEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TripEventRepository extends JpaRepository<TripEvent, String> {
    List<TripEvent> findAllByUser_Id(String userId);
}