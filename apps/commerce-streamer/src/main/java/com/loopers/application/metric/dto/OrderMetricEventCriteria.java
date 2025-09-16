package com.loopers.application.metric.dto;


import java.util.List;

public record OrderMetricEventCriteria(
        EventMessage eventMessage,
        List<OrderItem> orderItemList
) {
}
