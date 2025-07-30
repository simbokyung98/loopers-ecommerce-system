package com.loopers.application.like.dto;

public record LikeToggleInfo(
        boolean liked,
        Long totalLikeCount
) {
}
