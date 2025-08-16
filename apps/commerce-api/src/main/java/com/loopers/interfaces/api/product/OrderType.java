package com.loopers.interfaces.api.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {

    최신순("LATEST"),
    오래된순("OLDEST"),
    낮은가격순("PRICE_ASC"),
    높은가격순("PRICE_DESC"),
    낮은좋아요순("LIKE_ASC"),
    높은좋아요순("LIKE_DESC");

    private final String value;


    public static OrderType fromValue(String value) {
        for (OrderType type : OrderType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 정렬 타입입니다.");
    }

}
