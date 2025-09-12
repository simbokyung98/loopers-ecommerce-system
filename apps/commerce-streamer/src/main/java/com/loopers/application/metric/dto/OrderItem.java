package com.loopers.application.metric.dto;


public record OrderItem(
        Long productId,
        Long quantity
) {
}
