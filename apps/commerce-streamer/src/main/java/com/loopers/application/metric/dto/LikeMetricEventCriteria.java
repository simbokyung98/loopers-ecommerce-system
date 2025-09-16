package com.loopers.application.metric.dto;


import com.loopers.interfaces.consumer.event.LikeEventType;

public record LikeMetricEventCriteria(
        EventMessage eventMessage,
        Long userId,
        Long productId,
        String cacheKey,
        LikeEventType type
) {
}
