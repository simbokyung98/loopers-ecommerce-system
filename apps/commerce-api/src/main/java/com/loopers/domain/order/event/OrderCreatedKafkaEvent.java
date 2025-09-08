package com.loopers.domain.order.event;


import com.loopers.application.payment.event.ConfirmedOrderItem;

import java.util.List;

public record OrderCreatedKafkaEvent(Long orderId,
                                     Long userId,
                                     List<ConfirmedOrderItem> items) {

}
