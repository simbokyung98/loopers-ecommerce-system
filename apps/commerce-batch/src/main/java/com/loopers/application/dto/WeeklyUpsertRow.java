package com.loopers.application.dto;

public record WeeklyUpsertRow(
        Long productId,
        long viewSum,
        long likeSum,
        long orderSum,
        long score
) {}
