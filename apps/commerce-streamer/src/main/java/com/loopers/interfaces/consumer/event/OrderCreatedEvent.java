package com.loopers.interfaces.consumer.event;


public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        Long amount,
        PaymentType paymentType,
        String cardType,
         String cardNo
) {}
