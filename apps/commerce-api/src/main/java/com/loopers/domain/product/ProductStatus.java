package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductStatus{
    SELL("판매중"),
    OUT_OF_STOCK("품절"),
    DISCONTINUED("판매중지"),
    HIDDEN("숨김처리");

    private final String description;

    public static ProductStatus fromJson(String value) {
        for (ProductStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 상품 상태입니다.");
    }
}
