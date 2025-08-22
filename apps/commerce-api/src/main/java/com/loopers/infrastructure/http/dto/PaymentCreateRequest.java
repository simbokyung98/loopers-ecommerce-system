package com.loopers.infrastructure.http.dto;

public record PaymentCreateRequest(
        String orderId,
        String cardType,
        String cardNo,
        String amount
) {
    public PaymentGateWayCreateRequest toGateWayRequest(String callbackUrl){
        return new PaymentGateWayCreateRequest(
                orderId,
                cardType,
                cardNo,
                amount,
                callbackUrl
        );
    }
}
