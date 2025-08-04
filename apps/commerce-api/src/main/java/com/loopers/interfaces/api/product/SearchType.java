package com.loopers.interfaces.api.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SearchType {
    브랜드명("brandName"),
    상품명("productName");

    private final String value;

    public static SearchType fromValue(String value) {
        for (SearchType type : SearchType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 검색 조건 입니다.");
    }

}
