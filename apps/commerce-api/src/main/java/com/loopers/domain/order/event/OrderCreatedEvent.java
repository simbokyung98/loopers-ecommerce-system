package com.loopers.domain.order.event;


import com.loopers.domain.payment.PaymentType;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        Long amount,
        PaymentType paymentType,
        String cardType,
         String cardNo
) {}
