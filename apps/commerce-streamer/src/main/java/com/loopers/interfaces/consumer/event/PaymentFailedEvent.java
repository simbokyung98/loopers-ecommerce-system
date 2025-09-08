package com.loopers.interfaces.consumer.event;


public record PaymentFailedEvent(
        Long orderId,
        Long paymentId,
        Long userId
) {
}
