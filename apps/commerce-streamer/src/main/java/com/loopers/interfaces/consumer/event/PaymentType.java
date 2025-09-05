package com.loopers.interfaces.consumer.event;

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
                        new IllegalArgumentException("지원하지 않는 결제 타입입니다."));
    }



}
