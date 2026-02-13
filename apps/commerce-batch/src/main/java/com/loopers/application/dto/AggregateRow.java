package com.loopers.application.dto;

public record AggregateRow(
        Long productId,
        long viewSum,
        long likeSum,
        long orderSum
) {
}
