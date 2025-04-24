package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum TransactionType {
    EXPENSE("expense"),
    REVENUE("revenue"),
    ;

    private final String type;

    TransactionType(String type) {
        this.type = type;
    }
}
