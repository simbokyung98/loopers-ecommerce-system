package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PAID("결제완료"),
    PREPARING("배송준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("주문취소"),
    REFUNDED("환불");

    private final String description;

    public static OrderStatus fromJson(String value) {
        for (OrderStatus status : values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 주문 상태입니다.");
    }
}
