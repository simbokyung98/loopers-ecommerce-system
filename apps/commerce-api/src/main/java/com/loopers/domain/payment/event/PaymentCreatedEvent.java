package com.loopers.domain.payment.event;

public record PaymentCreatedEvent(
        Long orderId
) {
}
