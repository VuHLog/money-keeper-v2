package com.vuhlog.money_keeper.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class PeriodOfTime {
    private Timestamp startDate;
    private Timestamp endDate;
}
