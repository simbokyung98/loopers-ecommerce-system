package com.loopers.application.dto;

public record ScoredAggregate(
        Long productId,
        long viewSum,
        long likeSum,
        long orderSum,
        long score
) {
}
