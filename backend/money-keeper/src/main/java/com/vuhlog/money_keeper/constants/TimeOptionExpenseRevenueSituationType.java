package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum TimeOptionExpenseRevenueSituationType {
    PRESENT("Hiện tại"),
    MONTH("Tháng"),
    QUARTER("Quý"),
    YEAR("Năm"),
    OPTIONAL("Tùy chọn")
    ;

    private final String type;

    TimeOptionExpenseRevenueSituationType(String type) {
        this.type = type;
    }
}
