package com.loopers.infrastructure.http.dto;

public record PaymentGateWayCreateRequest(
        String orderId,
        String cardType,
        String cardNo,
        String amount,
        String callbackUrl
) {
}
