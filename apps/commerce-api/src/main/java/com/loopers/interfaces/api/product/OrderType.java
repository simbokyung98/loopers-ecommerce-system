package com.loopers.interfaces.api.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderType {

    최신순("latest"),
    오래된순("oldest"),
    낮은가격순("price_asc"),
    높은가격순("price_desc"),
    낮은좋아요순("like_asc"),
    높은좋아요순("like_desc");

    private final String value;


    public static OrderType fromValue(String value) {
        for (OrderType type : OrderType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 정렬 타입입니다.");
    }

}
