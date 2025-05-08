package com.vuhlog.money_keeper.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    PASSWORD_NOT_MATCH(1008, "Password does not match", HttpStatus.BAD_REQUEST),
    DICTIONARY_EXPENSE_NOT_EXISTED(2001, "Dictionary expense not existed", HttpStatus.NOT_FOUND),
    DICTIONARY_REVENUE_NOT_EXISTED(3001, "Dictionary revenue not existed", HttpStatus.NOT_FOUND),
    BANK_NOT_EXISTED(4001, "Bank not existed", HttpStatus.NOT_FOUND),
    EXPENSE_REGULAR_NOT_EXISTED(5001, "Expense regular not existed", HttpStatus.NOT_FOUND),
    REVENUE_REGULAR_NOT_EXISTED(6001, "Revenue regular not existed", HttpStatus.NOT_FOUND),
    BUCKET_PAYMENT_NOT_EXISTED(7001, "Bucket payment not existed", HttpStatus.NOT_FOUND),
    UPDATE_TIME_LIMIT(8001, "You can't update expense/revenue after 1 month", HttpStatus.BAD_REQUEST),
    EXPENSE_LIMIT_NOT_EXISTED(9001, "Expense limit not existed", HttpStatus.NOT_FOUND),
    EXPENSE_LIMIT_END_DATE_INVALID(9002, "Expense limit not existed", HttpStatus.BAD_REQUEST),
    DATE_LESS_THAN_TOMORROW(10001, "Date less than date tomorrow", HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_MONEY(10002, "Not enough money", HttpStatus.BAD_REQUEST),
    NOTIFICATION_NOT_EXISTED(11001, "Notification not existed", HttpStatus.BAD_REQUEST),
    ;
    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
