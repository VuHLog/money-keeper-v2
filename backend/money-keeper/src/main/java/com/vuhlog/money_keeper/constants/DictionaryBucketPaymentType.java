package com.vuhlog.money_keeper.constants;

import lombok.Getter;

@Getter
public enum DictionaryBucketPaymentType {
    CASH("Tiền mặt"),
    BANK("Tài khoản ngân hàng"),
    CREDIT_DEBIT_CARD("Thẻ tín dụng"),
    INVEST("Tài khoản đầu tư"),
    WALLET("Ví điện tử"),
    ORTHER("Khác")
    ;

    private final String type;

    DictionaryBucketPaymentType(String type) {
        this.type = type;
    }
}
