package com.loopers.application.payment.dto;


import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PaymentType;

public class PaymentInfo {
    public record PaymentResult(
            Long id,
            Long orderId,
            Long userId,
            Long amount,
            PaymentType type,
            PaymentStatus status
    ) {}

}
