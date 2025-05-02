package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum ReportTimeOptionType {
    MONTH("Theo tháng"),
    YEAR("Theo năm"),
    OPTIONAL("Tùy chọn"),
    ;

    private final String type;

    ReportTimeOptionType(String type) {
        this.type = type;
    }
}
