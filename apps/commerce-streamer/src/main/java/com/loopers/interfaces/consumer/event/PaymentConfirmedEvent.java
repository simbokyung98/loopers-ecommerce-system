package com.loopers.interfaces.consumer.event;

import java.util.List;

public record PaymentConfirmedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        List<ConfirmedOrderItem> items
) {}
