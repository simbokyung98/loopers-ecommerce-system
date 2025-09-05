package com.loopers.domain.Like.event;

public record LikeEvent(
        Long userId, Long productId, String cacheKey, LikeEventType type
) {
}
