package com.loopers.interfaces.consumer.event;

public record ConfirmedOrderItem(
        Long productId,
        Long quantity
) {}
