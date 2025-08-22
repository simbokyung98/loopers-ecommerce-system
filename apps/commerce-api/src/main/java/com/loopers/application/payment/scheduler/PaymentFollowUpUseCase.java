package com.loopers.application.payment.scheduler;

public interface PaymentFollowUpUseCase {
    void onCardPendingTick(Long orderId, Long paymentId, String txKey);

    void onCardPaymentCallback(Long orderId, String txKey);


}
