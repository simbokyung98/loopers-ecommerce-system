package com.loopers.application.payment.event;

public record ConfirmedOrderItem(
        Long productId,
        Long quantity
) {}
