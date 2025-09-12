package com.loopers.interfaces.consumer.event;

import com.loopers.application.metric.dto.OrderItem;

import java.util.List;

public record PaymentConfirmedEvent(
        Long orderId,
        Long paymentId,
        Long userId,
        List<ConfirmedOrderItem> items
) {
    public List<OrderItem> toOrderItemList(){
        return items.stream().map(ConfirmedOrderItem::to).toList();
    }

}
