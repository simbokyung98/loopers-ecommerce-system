package com.loopers.domain.payment;


public class PaymentCommand {

    public record CreateCardPayment(
            Long orderId,
            Long userId,
            PaymentType type,
            Long amount,
            String pgTxId

    ){}

    public record CreatePointPayment(
            Long orderId,
            Long userId,
            PaymentType type,
            Long amount,
            PaymentStatus status

    ){}


}
