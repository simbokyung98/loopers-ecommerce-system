package com.loopers.domain.payment;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import java.util.Arrays;

public enum PaymentType {
    POINT("POINT", "포인트 결제"),
    CARD("CARD", "카드 결제");

    private final String code;
    private final String description;

    PaymentType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PaymentType from(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() ->
                        new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 결제 타입입니다."));
    }



}
