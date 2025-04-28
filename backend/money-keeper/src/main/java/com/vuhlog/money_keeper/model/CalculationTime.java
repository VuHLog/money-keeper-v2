package com.vuhlog.money_keeper.model;

import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalculationTime {
    private LocalDate startDate;
    private LocalDate endOfStartMonth;
    private LocalDate startDateBetween;
    private LocalDate endDateBetween;
    private LocalDate startDateOfMonthEndDate;
    private LocalDate endDate;
}
