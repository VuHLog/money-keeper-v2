package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum TimeOptionType {
    TODAY("Hôm nay"),
    THIS_WEEK("Tuần này"),
    LAST_30_DAYS("30 ngày gần nhất"),
    THIS_MONTH("Tháng này"),
    LAST_MONTH("Tháng trước"),
    THIS_QUARTER("Quý này"),
    LAST_QUARTER("Quý trước"),
    THIS_YEAR("Năm nay"),
    LAST_YEAR("Năm trước"),
    FULL("Toàn bộ thời gian"),
    OPTIONAL("Tùy chọn")
    ;

    private final String type;

    TimeOptionType(String type) {
        this.type = type;
    }
}
