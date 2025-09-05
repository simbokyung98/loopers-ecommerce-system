package com.loopers.application.payment.event;

import java.util.List;

public record PaymentConfirmedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        List<ConfirmedOrderItem> items
) {}
