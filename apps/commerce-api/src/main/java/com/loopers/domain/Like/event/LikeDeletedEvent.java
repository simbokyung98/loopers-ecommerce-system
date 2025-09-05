package com.loopers.domain.Like.event;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LikeDeletedEvent(Long userId, Long productId) {
    public LikeEvent withCacheKeysAndType(String cacheKey , LikeEventType type) {
        return new LikeEvent(userId,productId , cacheKey, type);
    }
}
