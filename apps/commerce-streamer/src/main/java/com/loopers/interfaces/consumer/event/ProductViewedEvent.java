package com.loopers.interfaces.consumer.event;

import com.loopers.application.metric.dto.EventMessage;
import com.loopers.application.metric.dto.ProductMetricEventCriteria;

public record ProductViewedEvent(Long productId) {
    public ProductMetricEventCriteria to(EventMessage eventMessage){
        return new ProductMetricEventCriteria(eventMessage, productId);
    }
}
