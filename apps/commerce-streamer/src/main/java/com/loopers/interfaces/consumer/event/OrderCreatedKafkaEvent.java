package com.loopers.interfaces.consumer.event;


import java.util.List;

public record OrderCreatedKafkaEvent(Long orderId,
                                     Long userId,
                                     List<ConfirmedOrderItem> items) {

}
