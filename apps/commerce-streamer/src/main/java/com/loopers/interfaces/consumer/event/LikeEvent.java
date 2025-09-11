package com.loopers.interfaces.consumer.event;

import com.loopers.application.metric.dto.EventMessage;
import com.loopers.application.metric.dto.LikeMetricEventCriteria;

public record LikeEvent(
        Long userId, Long productId, String cacheKey, LikeEventType type
) {

    public LikeMetricEventCriteria to(EventMessage eventMessage){
        return new LikeMetricEventCriteria(eventMessage, userId, productId, cacheKey, type);
    }
}
