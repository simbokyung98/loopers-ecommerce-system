package com.loopers.application.metric.dto;


public record ProductMetricEventCriteria(
        EventMessage eventMessage,
        Long productId

) {
}
