package com.loopers.interfaces.consumer.event;

public record LikeEvent(
        Long userId, Long productId, String cacheKey, LikeEventType type
) {
}
