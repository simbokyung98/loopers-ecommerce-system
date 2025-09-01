package com.loopers.domain.payment.event;

public record PaymentFailedEvent(
        Long orderId
) {
}
