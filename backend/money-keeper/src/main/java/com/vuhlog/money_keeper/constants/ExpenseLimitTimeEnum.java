package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum ExpenseLimitTimeEnum {
    NO_REPEAT("Không lặp lại"),
    EVERY_DAY("Hàng ngày"),
    EVERY_WEEK("Hàng tuần"),
    EVERY_MONTH("Hàng tháng"),
    EVERY_QUARTER("Hàng quý"),
    EVERY_YEAR("Hàng năm"),
    ;

    private final String type;

    ExpenseLimitTimeEnum(String type) {
        this.type = type;
    }
}
