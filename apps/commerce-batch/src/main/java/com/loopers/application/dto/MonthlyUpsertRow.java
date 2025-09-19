package com.loopers.application.dto;
public record MonthlyUpsertRow(
        Long productId,
        long viewSum,
        long likeSum,
        long orderSum,
        long score
) {
}
