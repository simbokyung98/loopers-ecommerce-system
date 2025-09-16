package com.loopers.interfaces.consumer.event;

import com.loopers.application.metric.dto.OrderItem;

public record ConfirmedOrderItem(
        Long productId,
        Long quantity
) {
    public OrderItem to(){
        return new OrderItem(
                productId, quantity
        );
    }
}
