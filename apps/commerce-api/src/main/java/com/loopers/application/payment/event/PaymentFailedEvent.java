package com.loopers.application.payment.event;


public record PaymentFailedEvent(
        Long orderId,
        Long paymentId,
        Long userId
) {
}
